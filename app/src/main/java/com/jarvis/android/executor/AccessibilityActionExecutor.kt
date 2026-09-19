package com.jarvis.android.executor

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult
import com.jarvis.android.gestures.GestureBuilder
import com.jarvis.android.safety.ActionValidator
import kotlin.math.roundToInt

class AccessibilityActionExecutor(
    private val service: AccessibilityService,
    private val validator: ActionValidator = ActionValidator()
) : ActionExecutor {

    override fun execute(action: Action): ActionResult {
        val start = System.currentTimeMillis()
        val bounds = Rect(0, 0, service.resources.displayMetrics.widthPixels, service.resources.displayMetrics.heightPixels)
        val validation = validator.validate(action, bounds)
        if (validation.isFailure) {
            return ActionResult(action.id, false, validation.exceptionOrNull()?.message ?: "Invalid action")
        }

        val accepted = when (action) {
            is Action.Tap -> dispatchGesture(GestureBuilder.line(action.x, action.y, action.x, action.y, 60L))
            is Action.LongPress -> dispatchGesture(GestureBuilder.line(action.x, action.y, action.x, action.y, action.durationMs))
            is Action.Swipe -> dispatchGesture(GestureBuilder.line(action.startX, action.startY, action.endX, action.endY, action.durationMs))
            is Action.Scroll -> dispatchGesture(
                GestureBuilder.line(action.x, action.y, action.x + action.deltaX, action.y + action.deltaY, 320L)
            )
            is Action.TextInput -> false
            is Action.SystemKey -> when (action.key) {
                Action.Key.BACK -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                Action.Key.HOME -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                Action.Key.RECENTS -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            }
        }

        return ActionResult(action.id, accepted, if (accepted) "accepted" else "rejected", System.currentTimeMillis() - start)
    }

    private fun dispatchGesture(gesture: android.accessibilityservice.GestureDescription): Boolean =
        service.dispatchGesture(gesture, null, null)
}
