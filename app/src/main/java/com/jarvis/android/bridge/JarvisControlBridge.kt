package com.jarvis.android.bridge

import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult
import com.jarvis.android.accessibility.JarvisAccessibilityService
import com.jarvis.android.perception.AccessibilitySnapshotter
import com.jarvis.android.perception.UiSnapshot

/** Stable boundary for the existing JARVIS runtime. No cognition lives here. */
class JarvisControlBridge(
    private val snapshotter: AccessibilitySnapshotter = AccessibilitySnapshotter()
) {
    fun submit(action: Action): ActionResult {
        val service = JarvisAccessibilityService.current()
            ?: return ActionResult(action.id, false, "accessibility_service_unavailable")
        return service.execute(action)
    }

    /** Returns the current semantic screen state for cognition/planning. */
    fun observe(): UiSnapshot? {
        val service = JarvisAccessibilityService.current() ?: return null
        return snapshotter.capture(service.rootInActiveWindow)
    }
}
