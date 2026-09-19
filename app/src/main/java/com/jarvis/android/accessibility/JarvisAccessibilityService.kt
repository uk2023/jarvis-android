package com.jarvis.android.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {
    @Volatile
    var latestRoot: AccessibilityNodeInfo? = null
        private set

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        latestRoot = rootInActiveWindow
    }

    override fun onInterrupt() {
        latestRoot = null
    }
}
