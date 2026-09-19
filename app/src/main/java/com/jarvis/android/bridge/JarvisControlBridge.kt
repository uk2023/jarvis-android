package com.jarvis.android.bridge

import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult
import com.jarvis.android.accessibility.JarvisAccessibilityService

/** Stable boundary for the existing JARVIS runtime. No cognition lives here. */
class JarvisControlBridge {
    fun submit(action: Action): ActionResult {
        val service = JarvisAccessibilityService.current()
            ?: return ActionResult(action.id, false, "accessibility_service_unavailable")
        return service.execute(action)
    }
}
