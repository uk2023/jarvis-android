package com.jarvis.android

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The control layer is intentionally headless. Android exposes the
        // AccessibilityService through system settings; JARVIS drives actions
        // through the bridge rather than through this Activity UI.
        if (!isAccessibilityEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun isAccessibilityEnabled(): Boolean = false
}
