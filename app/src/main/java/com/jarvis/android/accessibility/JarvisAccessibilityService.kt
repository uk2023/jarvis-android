package com.jarvis.android.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult
import com.jarvis.android.gestures.HumanGestureTiming
import java.util.concurrent.atomic.AtomicReference

class JarvisAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { instance.set(this) }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    fun execute(action: Action): ActionResult {
        val start = System.currentTimeMillis()
        val ok = when (action) {
            is Action.Tap -> gesture(action.x, action.y, action.x, action.y, HumanGestureTiming.tapMs())
            is Action.LongPress -> gesture(action.x, action.y, action.x, action.y, HumanGestureTiming.longPressMs(action.durationMs))
            is Action.Swipe -> gesture(action.startX, action.startY, action.endX, action.endY, HumanGestureTiming.swipeMs(action.durationMs))
            is Action.Scroll -> gesture(action.x, action.y, action.x + action.deltaX, action.y + action.deltaY, HumanGestureTiming.scrollMs())
            is Action.TextInput -> inputText(action.text)
            is Action.SystemKey -> performGlobalAction(when (action.key) {
                Action.Key.BACK -> GLOBAL_ACTION_BACK
                Action.Key.HOME -> GLOBAL_ACTION_HOME
                Action.Key.RECENTS -> GLOBAL_ACTION_RECENTS
            })
        }
        return ActionResult(action.id, ok, if (ok) "executed" else "execution_failed", System.currentTimeMillis() - start)
    }

    private fun gesture(sx: Float, sy: Float, ex: Float, ey: Float, duration: Long): Boolean {
        if (listOf(sx, sy, ex, ey).any { it < 0f }) return false
        val path = Path().apply { moveTo(sx, sy); lineTo(ex, ey) }
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    private fun inputText(text: String): Boolean {
        val node = focusedEditable(rootInActiveWindow) ?: return false
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun focusedEditable(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null
        if (root.isEditable && root.isFocused) return root
        for (i in 0 until root.childCount) focusedEditable(root.getChild(i))?.let { return it }
        return null
    }

    companion object {
        private val instance = AtomicReference<JarvisAccessibilityService?>()
        fun current(): JarvisAccessibilityService? = instance.get()
    }
}
