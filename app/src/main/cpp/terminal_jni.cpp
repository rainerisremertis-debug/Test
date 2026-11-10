#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <pty.h>
#include <termios.h>
#include <signal.h>
#include <cstring>
#include <map>
#include <memory>

#define LOG_TAG "TerminalJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Session management
struct TerminalSession {
    int master_fd;
    int slave_fd;
    pid_t child_pid;
    char* home_dir;
    char* cache_dir;
    std::map<std::string, std::string> env_vars;
    std::string output_buffer;
};

static std::map<long, std::unique_ptr<TerminalSession>> sessions;
static long next_session_id = 1;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_termuxai_terminal_utils_NativeTerminal_createSession(
    JNIEnv* env, jobject thiz, jstring homeDir, jstring cacheDir) {

    const char* home = env->GetStringUTFChars(homeDir, nullptr);
    const char* cache = env->GetStringUTFChars(cacheDir, nullptr);

    auto session = std::make_unique<TerminalSession>();
    session->home_dir = strdup(home);
    session->cache_dir = strdup(cache);

    // Create PTY
    if (openpty(&session->master_fd, &session->slave_fd, nullptr, nullptr, nullptr) == -1) {
        LOGE("Failed to create PTY");
        env->ReleaseStringUTFChars(homeDir, home);
        env->ReleaseStringUTFChars(cacheDir, cache);
        return -1;
    }

    // Set terminal attributes
    struct termios tios;
    tcgetattr(session->slave_fd, &tios);
    cfmakeraw(&tios);
    tios.c_lflag |= ECHO | ICANON;
    tcsetattr(session->slave_fd, TCSANOW, &tios);

    // Fork child process
    session->child_pid = fork();
    if (session->child_pid == 0) {
        // Child process
        close(session->master_fd);
        setsid();
        ioctl(session->slave_fd, TIOCSCTTY, 0);
        dup2(session->slave_fd, STDIN_FILENO);
        dup2(session->slave_fd, STDOUT_FILENO);
        dup2(session->slave_fd, STDERR_FILENO);
        close(session->slave_fd);

        // Set environment
        setenv("HOME", session->home_dir, 1);
        setenv("TERM", "xterm-256color", 1);
        setenv("PATH", "/system/bin:/system/xbin:/data/data/com.termuxai.terminal/files/home/bin", 1);
        setenv("LANG", "en_US.UTF-8", 1);

        // Execute shell
        execl("/system/bin/sh", "sh", nullptr);
        _exit(1);
    } else if (session->child_pid < 0) {
        LOGE("Failed to fork");
        close(session->master_fd);
        close(session->slave_fd);
        env->ReleaseStringUTFChars(homeDir, home);
        env->ReleaseStringUTFChars(cacheDir, cache);
        return -1;
    }

    // Parent process
    close(session->slave_fd);
    session->slave_fd = -1;

    // Set non-blocking
    int flags = fcntl(session->master_fd, F_GETFL, 0);
    fcntl(session->master_fd, F_SETFL, flags | O_NONBLOCK);

    long id = next_session_id++;
    sessions[id] = std::move(session);

    env->ReleaseStringUTFChars(homeDir, home);
    env->ReleaseStringUTFChars(cacheDir, cache);

    LOGI("Created session %ld", id);
    return id;
}

JNIEXPORT jstring JNICALL
Java_com_termuxai_terminal_utils_NativeTerminal_executeCommand(
    JNIEnv* env, jobject thiz, jlong sessionId, jstring command) {

    auto it = sessions.find(sessionId);
    if (it == sessions.end()) {
        return env->NewStringUTF("Error: Session not found");
    }

    auto& session = it->second;
    const char* cmd = env->GetStringUTFChars(command, nullptr);

    // Write command to PTY
    std::string cmdStr(cmd);
    cmdStr += "\n";
    write(session->master_fd, cmdStr.c_str(), cmdStr.length());

    env->ReleaseStringUTFChars(command, cmd);

    // Read output (with timeout)
    char buffer[4096];
    std::string output;
    struct timeval tv;
    fd_set rfds;

    for (int i = 0; i < 50; i++) { // 5 seconds max
        FD_ZERO(&rfds);
        FD_SET(session->master_fd, &rfds);
        tv.tv_sec = 0;
        tv.tv_usec = 100000; // 100ms

        int retval = select(session->master_fd + 1, &rfds, nullptr, nullptr, &tv);
        if (retval > 0) {
            ssize_t n = read(session->master_fd, buffer, sizeof(buffer) - 1);
            if (n > 0) {
                buffer[n] = '\0';
                output += buffer;
            } else if (n == 0) {
                break;
            }
        } else {
            // No more data available
            if (!output.empty()) break;
        }
    }

    if (output.empty()) {
        output = "Command executed (no output)\n";
    }

    return env->NewStringUTF(output.c_str());
}

JNIEXPORT void JNICALL
Java_com_termuxai_terminal_utils_NativeTerminal_clearSession(
    JNIEnv* env, jobject thiz, jlong sessionId) {

    auto it = sessions.find(sessionId);
    if (it != sessions.end()) {
        it->second->output_buffer.clear();
    }
}

JNIEXPORT void JNICALL
Java_com_termuxai_terminal_utils_NativeTerminal_destroySession(
    JNIEnv* env, jobject thiz, jlong sessionId) {

    auto it = sessions.find(sessionId);
    if (it != sessions.end()) {
        auto& session = it->second;
        if (session->child_pid > 0) {
            kill(session->child_pid, SIGTERM);
            waitpid(session->child_pid, nullptr, 0);
        }
        close(session->master_fd);
        free(session->home_dir);
        free(session->cache_dir);
        sessions.erase(it);
        LOGI("Destroyed session %ld", sessionId);
    }
}

JNIEXPORT void JNICALL
Java_com_termuxai_terminal_utils_NativeTerminal_setEnvironment(
    JNIEnv* env, jobject thiz, jlong sessionId, jstring key, jstring value) {

    auto it = sessions.find(sessionId);
    if (it != sessions.end()) {
        const char* k = env->GetStringUTFChars(key, nullptr);
        const char* v = env->GetStringUTFChars(value, nullptr);
        it->second->env_vars[std::string(k)] = std::string(v);
        env->ReleaseStringUTFChars(key, k);
        env->ReleaseStringUTFChars(value, v);
    }
}

JNIEXPORT void JNICALL
Java_com_termuxai_terminal_utils_NativeTerminal_saveSessionState(
    JNIEnv* env, jobject thiz, jlong sessionId) {
    // Save session state to file
    LOGI("Saving state for session %ld", sessionId);
}

JNIEXPORT void JNICALL
Java_com_termuxai_terminal_utils_NativeTerminal_restoreSessionState(
    JNIEnv* env, jobject thiz, jlong sessionId) {
    // Restore session state from file
    LOGI("Restoring state for session %ld", sessionId);
}

JNIEXPORT jstring JNICALL
Java_com_termuxai_terminal_utils_NativeTerminal_getTerminalOutput(
    JNIEnv* env, jobject thiz, jlong sessionId) {

    auto it = sessions.find(sessionId);
    if (it != sessions.end()) {
        return env->NewStringUTF(it->second->output_buffer.c_str());
    }
    return env->NewStringUTF("");
}

} // extern "C"