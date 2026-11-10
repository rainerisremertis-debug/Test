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
#include <vector>

#define LOG_TAG "ShellSession"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Shell session implementation
// This provides a Unix-like shell environment

struct ShellSession {
    int master_fd;
    pid_t shell_pid;
    std::string home_dir;
    std::string current_dir;
    std::map<std::string, std::string> env;
    std::vector<std::string> history;
    bool is_running;
};

// Command history management
void addToHistory(ShellSession* session, const std::string& command) {
    if (!command.empty() && (session->history.empty() || session->history.back() != command)) {
        session->history.push_back(command);
        if (session->history.size() > 1000) {
            session->history.erase(session->history.begin());
        }
    }
}

// Environment variable management
std::string getEnv(ShellSession* session, const std::string& key) {
    auto it = session->env.find(key);
    if (it != session->env.end()) return it->second;
    char* val = getenv(key.c_str());
    return val ? std::string(val) : "";
}

void setEnv(ShellSession* session, const std::string& key, const std::string& value) {
    session->env[key] = value;
    setenv(key.c_str(), value.c_str(), 1);
}

// Current directory management
std::string getCurrentDir(ShellSession* session) {
    return session->current_dir.empty() ? session->home_dir : session->current_dir;
}

void setCurrentDir(ShellSession* session, const std::string& dir) {
    session->current_dir = dir;
    chdir(dir.c_str());
}

// Command parsing
std::vector<std::string> parseCommand(const std::string& command) {
    std::vector<std::string> args;
    std::string current;
    bool inQuotes = false;
    char quoteChar = '\0';

    for (char c : command) {
        if (inQuotes) {
            if (c == quoteChar) {
                inQuotes = false;
            } else {
                current += c;
            }
        } else {
            if (c == '"' || c == '\'') {
                inQuotes = true;
                quoteChar = c;
            } else if (c == ' ' || c == '\t') {
                if (!current.empty()) {
                    args.push_back(current);
                    current.clear();
                }
            } else {
                current += c;
            }
        }
    }
    if (!current.empty()) {
        args.push_back(current);
    }
    return args;
}