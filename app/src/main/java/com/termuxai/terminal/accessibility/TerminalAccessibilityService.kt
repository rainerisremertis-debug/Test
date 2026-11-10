package com.termuxai.terminal.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.termuxai.terminal.ai.AIModelManager
import com.termuxai.terminal.utils.GotchaIntegration

class TerminalAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: TerminalAccessibilityService? = null
        fun getInstance(): TerminalAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
            notificationTimeout = 100
        }
        serviceInfo = info

        // Initialize Gotcha AI integration
        GotchaIntegration.initialize(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // Handle window changes for screen reading
                handleWindowChange(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                // Handle clicks for automation
                handleViewClick(event)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                // Handle text changes
                handleTextChange(event)
            }
        }
    }

    private fun handleWindowChange(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        // Read screen content for AI processing
        val screenText = extractTextFromNode(rootNode)
        // Send to AI for analysis if needed
        GotchaIntegration.processScreenContent(screenText)
    }

    private fun handleViewClick(event: AccessibilityEvent) {
        // Track user interactions for skill learning
    }

    private fun handleTextChange(event: AccessibilityEvent) {
        // Track text input for context
    }

    private fun extractTextFromNode(node: AccessibilityNodeInfo): String {
        val builder = StringBuilder()
        if (node.text != null) {
            builder.append(node.text).append("\n")
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                builder.append(extractTextFromNode(child))
            }
        }
        return builder.toString()
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun performClick(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun performTextInput(node: AccessibilityNodeInfo, text: String): Boolean {
        val arguments = android.os.Bundle()
        arguments.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        return findNodeByTextRecursive(rootNode, text)
    }

    private fun findNodeByTextRecursive(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findNodeByTextRecursive(child, text)?.let { return it }
            }
        }
        return null
    }
}