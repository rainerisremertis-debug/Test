package com.termuxai.terminal.utils

class NativeTerminal {
    companion object {
        init {
            System.loadLibrary("terminal")
        }
    }

    external fun createSession(homeDir: String, cacheDir: String): Long
    external fun executeCommand(sessionId: Long, command: String): String
    external fun clearSession(sessionId: Long)
    external fun destroySession(sessionId: Long)
    external fun setEnvironment(sessionId: Long, key: String, value: String)
    external fun saveSessionState(sessionId: Long)
    external fun restoreSessionState(sessionId: Long)
    external fun getTerminalOutput(sessionId: Long): String
}