package com.jarvis.android.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult
import com.jarvis.android.executor.ActionExecutionPolicy
import com.jarvis.android.executor.ScreenStateFingerprint
import com.jarvis.android.gestures.GestureBuilder
import com.jarvis.android.semantic.SemanticUiController
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class JarvisAccessibilityService : AccessibilityService() {
    private lateinit var semanticUi: SemanticUiController
    private val policy = ActionExecutionPolicy()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        instance.set(this)
        semanticUi = SemanticUiController(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    fun execute(action: Action): ActionResult {
        if (action is Action.SemanticClick || action is Action.SemanticScroll) {
            if (!::semanticUi.isInitialized) return ActionResult(action.id, false, "service_not_ready", retryable = true)
            return semanticUi.execute(action)
        }

        val started = System.currentTimeMillis()
        val before = ScreenStateFingerprint.capture(rootInActiveWindow)
        val maxAttempts = policy.attemptsFor(action)
        var lastMessage = "execution_failed"

        for (attempt in 1..maxAttempts) {
            val accepted = when (action) {
                is Action.Tap -> dispatchGestureAwaited(GestureBuilder.line(action.x, action.y, action.x, action.y, policy.duration(action)))
                is Action.LongPress -> dispatchGestureAwaited(GestureBuilder.line(action.x, action.y, action.x, action.y, policy.duration(action)))
                is Action.Swipe -> dispatchGestureAwaited(GestureBuilder.interpolatedLine(action.startX, action.startY, action.endX, action.endY, policy.duration(action)))
                is Action.Scroll -> dispatchGestureAwaited(GestureBuilder.interpolatedLine(action.x, action.y, action.x + action.deltaX, action.y + action.deltaY, policy.duration(action)))
                is Action.TextInput -> dispatchTextInput(action.text)
                is Action.SystemKey -> dispatchSystemKey(action.key)
                is Action.SemanticClick, is Action.SemanticScroll -> false
            }

            if (!accepted) {
                lastMessage = "attempt_${attempt}_failed"
                if (!policy.retryable(action) || attempt == maxAttempts) break
                continue
            }

            val verification = verifyAfterAction(action, before)
            if (verification.second != ActionResult.Verification.STALE_SCREEN) {
                return ActionResult(
                    action.id,
                    true,
                    verification.first,
                    System.currentTimeMillis() - started,
                    strategyFor(action),
                    false,
                    attempt,
                    verification.second
                )
            }

            lastMessage = "stale_screen_after_attempt_$attempt"
            if (!policy.retryable(action) || attempt == maxAttempts) break
        }

        return ActionResult(
            action.id,
            false,
            lastMessage,
            System.currentTimeMillis() - started,
            strategyFor(action),
            policy.retryable(action),
            maxAttempts,
            ActionResult.Verification.STALE_SCREEN.takeIf { lastMessage.startsWith("stale_screen") }
                ?: ActionResult.Verification.FAILED
        )
    }

    private fun dispatchGestureAwaited(gesture: GestureDescription): Boolean {
        if (Looper.myLooper() == Looper.getMainLooper()) return dispatchGesture(gesture, null, null)
        val done = CountDownLatch(1)
        val completed = AtomicReference(false)
        val callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                completed.set(true)
                done.countDown()
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                completed.set(false)
                done.countDown()
            }
        }
        if (!dispatchGesture(gesture, callback, mainHandler)) return false
        done.await(2_000L, TimeUnit.MILLISECONDS)
        return completed.get()
    }

    private fun dispatchTextInput(text: String): Boolean {
        val node = focusedEditable(rootInActiveWindow) ?: return false
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun dispatchSystemKey(key: Action.Key): Boolean = performGlobalAction(when (key) {
        Action.Key.BACK -> GLOBAL_ACTION_BACK
        Action.Key.HOME -> GLOBAL_ACTION_HOME
        Action.Key.RECENTS -> GLOBAL_ACTION_RECENTS
    })

    private fun verifyAfterAction(action: Action, before: Long): Pair<String, ActionResult.Verification> {
        if (Looper.myLooper() == Looper.getMainLooper()) return "accepted_pending_verification" to ActionResult.Verification.ACCEPTED_PENDING_VERIFICATION
        if (action is Action.TextInput || action is Action.SystemKey) return "executed_verified" to ActionResult.Verification.VERIFIED
        val after = ScreenStateFingerprint.capture(rootInActiveWindow)
        val changed = before != 0L && after != 0L && before != after
        return if (changed || action is Action.Tap || action is Action.LongPress) {
            "executed_verified" to ActionResult.Verification.VERIFIED
        } else {
            "executed_but_screen_unchanged" to ActionResult.Verification.STALE_SCREEN
        }
    }

    private fun strategyFor(action: Action): String = when (action) {
        is Action.Tap -> "gesture:tap"
        is Action.LongPress -> "gesture:long_press"
        is Action.Swipe -> "gesture:interpolated_swipe"
        is Action.Scroll -> "gesture:interpolated_scroll"
        is Action.TextInput -> "accessibility:set_text"
        is Action.SystemKey -> "global_action"
        is Action.SemanticClick -> "semantic"
        is Action.SemanticScroll -> "semantic"
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
