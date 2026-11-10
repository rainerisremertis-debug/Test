package com.termuxai.terminal.utils

import android.content.Context
import android.os.PowerManager
import android.util.Log

object ThermalManager {
    private const val TAG = "ThermalManager"
    private lateinit var context: Context
    private var thermalStatus = 0

    fun initialize(ctx: Context) {
        context = ctx
        // Monitor thermal status
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.addThermalStatusListener { status ->
                thermalStatus = status
                handleThermalStatus(status)
            }
        }
    }

    private fun handleThermalStatus(status: Int) {
        when (status) {
            PowerManager.THERMAL_STATUS_NONE -> {
                Log.d(TAG, "Thermal: Normal")
            }
            PowerManager.THERMAL_STATUS_LIGHT -> {
                Log.d(TAG, "Thermal: Light - reducing performance")
                reducePerformance(0.8f)
            }
            PowerManager.THERMAL_STATUS_MODERATE -> {
                Log.d(TAG, "Thermal: Moderate - reducing performance significantly")
                reducePerformance(0.5f)
            }
            PowerManager.THERMAL_STATUS_SEVERE -> {
                Log.d(TAG, "Thermal: Severe - minimal performance")
                reducePerformance(0.3f)
            }
            PowerManager.THERMAL_STATUS_CRITICAL -> {
                Log.d(TAG, "Thermal: Critical - pausing processing")
                pauseProcessing()
            }
            PowerManager.THERMAL_STATUS_EMERGENCY -> {
                Log.d(TAG, "Thermal: Emergency - emergency shutdown")
                emergencyShutdown()
            }
        }
    }

    private fun reducePerformance(factor: Float) {
        // Adjust thread counts, batch sizes, etc.
        // In production, communicate with native layer
    }

    private fun pauseProcessing() {
        // Pause AI model inference
    }

    private fun emergencyShutdown() {
        // Save state and shut down
        StateSavingService.saveState("emergency", "emergency_shutdown")
    }

    fun getThermalStatus(): Int = thermalStatus

    fun isThrottling(): Boolean = thermalStatus > PowerManager.THERMAL_STATUS_NONE
}