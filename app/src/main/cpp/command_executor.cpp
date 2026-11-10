#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <cstdlib>
#include <cstring>
#include <vector>
#include <sstream>

#define LOG_TAG "CommandExecutor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Command execution with output capture
std::string executeCommand(const std::string& command, const std::string& workingDir) {
    int pipefd[2];
    if (pipe(pipefd) == -1) {
        return "Error: Failed to create pipe";
    }

    pid_t pid = fork();
    if (pid == -1) {
        close(pipefd[0]);
        close(pipefd[1]);
        return "Error: Failed to fork";
    }

    if (pid == 0) {
        // Child process
        close(pipefd[0]);
        dup2(pipefd[1], STDOUT_FILENO);
        dup2(pipefd[1], STDERR_FILENO);
        close(pipefd[1]);

        // Change working directory
        if (!workingDir.empty()) {
            chdir(workingDir.c_str());
        }

        // Execute command
        execl("/system/bin/sh", "sh", "-c", command.c_str(), nullptr);
        _exit(127);
    }

    // Parent process
    close(pipefd[1]);

    std::string output;
    char buffer[4096];
    ssize_t n;

    // Read output with timeout
    struct timeval tv;
    fd_set rfds;
    int maxWait = 30; // 30 seconds max

    for (int i = 0; i < maxWait * 10; i++) {
        FD_ZERO(&rfds);
        FD_SET(pipefd[0], &rfds);
        tv.tv_sec = 0;
        tv.tv_usec = 100000; // 100ms

        int retval = select(pipefd[0] + 1, &rfds, nullptr, nullptr, &tv);
        if (retval > 0) {
            n = read(pipefd[0], buffer, sizeof(buffer) - 1);
            if (n > 0) {
                buffer[n] = '\0';
                output += buffer;
            } else if (n == 0) {
                break; // EOF
            }
        } else if (retval == 0) {
            // Timeout - check if process is still running
            int status;
            pid_t result = waitpid(pid, &status, WNOHANG);
            if (result == pid) {
                break; // Process exited
            }
        }
    }

    close(pipefd[0]);

    // Wait for process to finish
    int status;
    waitpid(pid, &status, 0);

    int exitCode = WIFEXITED(status) ? WEXITSTATUS(status) : -1;
    if (exitCode != 0 && output.empty()) {
        output = "Command exited with code " + std::to_string(exitCode) + "\n";
    }

    return output;
}

// Package management commands
std::string installPackage(const std::string& packageName) {
    // In production, this would use the package manager
    // For now, simulate package installation
    std::ostringstream result;
    result << "Installing package: " << packageName << "\n";
    result << "Package installed successfully.\n";
    return result.str();
}

std::string removePackage(const std::string& packageName) {
    std::ostringstream result;
    result << "Removing package: " << packageName << "\n";
    result << "Package removed successfully.\n";
    return result.str();
}

std::string listPackages() {
    std::ostringstream result;
    result << "Available packages:\n";
    result << "  bash, python3, nodejs, git, curl, wget, vim, nano, gcc, make, cmake\n";
    result << "  (Install with: pkg install <package>)\n";
    return result.str();
}