package com.termuxai.terminal.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.termuxai.terminal.TerminalApplication
import kotlinx.coroutines.*
import java.io.File
import java.io.ObjectOutputStream
import java.io.Serializable

class StateSavingService : Service() {

    companion object {
        private const val TAG = "StateSavingService"
        private const val STATE_DIR = "saved_states"
        private const val CAPACITY_THRESHOLD = 0.5f // 50% capacity
        private var instance: StateSavingService? = null

        fun initialize(context: Context) {
            // Start the service
            val intent = Intent(context, StateSavingService::class.java)
            context.startForegroundService(intent)
        }

        fun getInstance(): StateSavingService? = instance

        fun saveState(key: String, state: Serializable) {
            instance?.saveStateInternal(key, state)
        }

        fun restoreState(key: String): Any? {
            return instance?.restoreStateInternal(key)
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val stateDir by lazy { File(filesDir, STATE_DIR).apply { mkdirs() } }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoring() {
        scope.launch {
            while (isActive) {
                // Monitor system resources
                val memoryUsage = getMemoryUsage()
                val storageUsage = getStorageUsage()

                // Check if we've reached 50% capacity
                if (memoryUsage >= CAPACITY_THRESHOLD || storageUsage >= CAPACITY_THRESHOLD) {
                    Log.d(TAG, "Capacity threshold reached: memory=$memoryUsage, storage=$storageUsage")
                    saveAllStates()
                }

                delay(5000) // Check every 5 seconds
            }
        }
    }

    private fun getMemoryUsage(): Float {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        return usedMemory.toFloat() / maxMemory.toFloat()
    }

    private fun getStorageUsage(): Float {
        val filesDir = filesDir
        val totalSpace = filesDir.totalSpace
        val freeSpace = filesDir.freeSpace
        val usedSpace = totalSpace - freeSpace
        return usedSpace.toFloat() / totalSpace.toFloat()
    }

    private fun saveAllStates() {
        Log.d(TAG, "Saving all session states...")
        // Notify all active sessions to save state
        // In production, use a broadcast or callback mechanism
    }

    private fun saveStateInternal(key: String, state: Serializable) {
        try {
            val stateFile = File(stateDir, "$key.state")
            ObjectOutputStream(stateFile.outputStream()).use { oos ->
                oos.writeObject(state)
            }
            Log.d(TAG, "State saved: $key")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save state: $key", e)
        }
    }

    private fun restoreStateInternal(key: String): Any? {
        return try {
            val stateFile = File(stateDir, "$key.state")
            if (stateFile.exists()) {
                java.io.ObjectInputStream(stateFile.inputStream()).use { ois ->
                    ois.readObject()
                }
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore state: $key", e)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        scope.cancel()
    }
}