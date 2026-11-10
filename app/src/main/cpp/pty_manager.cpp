#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <fcntl.h>
#include <pty.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <cstring>
#include <string>

#define LOG_TAG "PtyManager"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// PTY (Pseudo-Terminal) management for terminal emulation

struct PtyPair {
    int master_fd;
    int slave_fd;
    char slave_name[256];
    bool is_open;
};

// Open a new PTY pair
PtyPair* openPty() {
    PtyPair* pty = new PtyPair();
    pty->is_open = false;

    if (openpty(&pty->master_fd, &pty->slave_fd, pty->slave_name, nullptr, nullptr) == -1) {
        LOGE("Failed to open PTY");
        delete pty;
        return nullptr;
    }

    // Set terminal attributes
    struct termios tios;
    tcgetattr(pty->slave_fd, &tios);
    cfmakeraw(&tios);
    tios.c_iflag |= ICRNL;
    tios.c_oflag |= OPOST | ONLCR;
    tios.c_lflag |= ECHO | ICANON | ISIG | IEXTEN;
    tios.c_cc[VMIN] = 1;
    tios.c_cc[VTIME] = 0;
    tcsetattr(pty->slave_fd, TCSANOW, &tios);

    // Set window size
    struct winsize ws;
    ws.ws_row = 24;
    ws.ws_col = 80;
    ws.ws_xpixel = 0;
    ws.ws_ypixel = 0;
    ioctl(pty->slave_fd, TIOCSWINSZ, &ws);

    pty->is_open = true;
    LOGI("PTY opened: %s", pty->slave_name);
    return pty;
}

// Close PTY pair
void closePty(PtyPair* pty) {
    if (pty && pty->is_open) {
        close(pty->master_fd);
        close(pty->slave_fd);
        pty->is_open = false;
        delete pty;
        LOGI("PTY closed");
    }
}

// Read from PTY master
ssize_t readPty(PtyPair* pty, char* buffer, size_t size) {
    if (!pty || !pty->is_open) return -1;
    return read(pty->master_fd, buffer, size);
}

// Write to PTY master
ssize_t writePty(PtyPair* pty, const char* data, size_t size) {
    if (!pty || !pty->is_open) return -1;
    return write(pty->master_fd, data, size);
}

// Resize PTY
int resizePty(PtyPair* pty, int rows, int cols) {
    if (!pty || !pty->is_open) return -1;
    struct winsize ws;
    ws.ws_row = rows;
    ws.ws_col = cols;
    ws.ws_xpixel = 0;
    ws.ws_ypixel = 0;
    return ioctl(pty->master_fd, TIOCSWINSZ, &ws);
}

// Set non-blocking mode
int setNonBlocking(PtyPair* pty, bool nonBlocking) {
    if (!pty || !pty->is_open) return -1;
    int flags = fcntl(pty->master_fd, F_GETFL, 0);
    if (nonBlocking) {
        flags |= O_NONBLOCK;
    } else {
        flags &= ~O_NONBLOCK;
    }
    return fcntl(pty->master_fd, F_SETFL, flags);
}