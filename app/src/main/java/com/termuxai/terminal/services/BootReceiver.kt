package com.termuxai.terminal.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.termuxai.terminal.TerminalApplication

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Restart services after boot
            StateSavingService.initialize(context)
            // Restore saved states
            // In production, restore terminal sessions, AI model states, etc.
        }
    }
}