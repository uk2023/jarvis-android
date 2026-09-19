package com.jarvis.android.input

import android.view.accessibility.AccessibilityNodeInfo
import android.os.Bundle

object TextInputController {
    fun setText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (!node.isEditable) return false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }
}
