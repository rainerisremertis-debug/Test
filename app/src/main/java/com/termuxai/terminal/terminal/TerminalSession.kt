package com.termuxai.terminal.terminal

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.termuxai.terminal.utils.NativeTerminal
import java.io.File

class TerminalSession(private val context: Context) {
    private val nativeTerminal = NativeTerminal()
    private val handler = Handler(Looper.getMainLooper())
    private var sessionId: Long = 0

    init {
        // Initialize native terminal session
        sessionId = nativeTerminal.createSession(
            context.filesDir.absolutePath,
            context.cacheDir.absolutePath
        )
        // Set up environment
        setupEnvironment()
    }

    private fun setupEnvironment() {
        // Create home directory
        val homeDir = File(context.filesDir, "home")
        homeDir.mkdirs()

        // Create bin directory
        val binDir = File(homeDir, "bin")
        binDir.mkdirs()

        // Set up PATH
        nativeTerminal.setEnvironment(sessionId, "HOME", homeDir.absolutePath)
        nativeTerminal.setEnvironment(sessionId, "PATH", "${binDir.absolutePath}:/system/bin:/system/xbin")
        nativeTerminal.setEnvironment(sessionId, "TERM", "xterm-256color")
        nativeTerminal.setEnvironment(sessionId, "LANG", "en_US.UTF-8")
    }

    fun execute(command: String, callback: (String) -> Unit) {
        Thread {
            val result = nativeTerminal.executeCommand(sessionId, command)
            handler.post { callback(result) }
        }.start()
    }

    fun clear() {
        nativeTerminal.clearSession(sessionId)
    }

    fun destroy() {
        nativeTerminal.destroySession(sessionId)
    }

    fun saveState() {
        nativeTerminal.saveSessionState(sessionId)
    }

    fun restoreState() {
        nativeTerminal.restoreSessionState(sessionId)
    }
}