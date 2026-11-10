package com.termuxai.terminal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import com.termuxai.terminal.services.StateSavingService
import com.termuxai.terminal.utils.ThermalManager

class TerminalApplication : Application(), Configuration.Provider {

    companion object {
        const val CHANNEL_AI_PROCESSING = "ai_processing"
        const val CHANNEL_TERMINAL = "terminal"
        const val CHANNEL_STATE_SAVING = "state_saving"
        lateinit var instance: TerminalApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Create notification channels
        createNotificationChannels()

        // Initialize thermal manager
        ThermalManager.initialize(this)

        // Initialize state saving service
        StateSavingService.initialize(this)

        // Initialize AI backend
        AIModelManager.initialize(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_AI_PROCESSING,
                    "AI Processing",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notifications for AI model processing"
                },
                NotificationChannel(
                    CHANNEL_TERMINAL,
                    "Terminal",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Terminal session notifications"
                },
                NotificationChannel(
                    CHANNEL_STATE_SAVING,
                    "State Saving",
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = "Session state saving notifications"
                }
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}