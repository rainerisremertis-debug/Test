package com.termuxai.terminal.utils

import android.content.Context
import android.content.Intent
import android.util.Log

object GotchaIntegration {
    private const val TAG = "GotchaIntegration"
    private const val GOTCHA_PACKAGE = "com.gotcha"
    private var context: Context? = null

    fun initialize(ctx: Context) {
        context = ctx
        Log.d(TAG, "Gotcha AI integration initialized")
    }

    fun processScreenContent(screenText: String) {
        // Send screen content to Gotcha for AI processing
        // In production, use AIDL or broadcast
        Log.d(TAG, "Processing screen content: ${screenText.take(100)}...")
    }

    fun sendCommandToGotcha(command: String) {
        context?.let { ctx ->
            try {
                val intent = Intent().apply {
                    setPackage(GOTCHA_PACKAGE)
                    action = "com.gotcha.ACTION_COMMAND"
                    putExtra("command", command)
                }
                ctx.sendBroadcast(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send command to Gotcha", e)
            }
        }
    }

    fun isGotchaInstalled(): Boolean {
        return try {
            context?.packageManager?.getPackageInfo(GOTCHA_PACKAGE, 0) != null
        } catch (e: Exception) {
            false
        }
    }

    fun requestAccessibilityPermission() {
        // Guide user to enable accessibility service
    }
}