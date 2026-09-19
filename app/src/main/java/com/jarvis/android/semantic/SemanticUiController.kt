package com.jarvis.android.semantic

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult
import com.jarvis.android.accessibility.JarvisAccessibilityService
import com.jarvis.android.gestures.HumanGestureTiming

/** Resolves human-readable UI targets against the live accessibility tree. */
class SemanticUiController(
    private val service: JarvisAccessibilityService
) {
    fun execute(action: Action): ActionResult {
        val start = System.currentTimeMillis()
        val root = service.rootInActiveWindow
            ?: return ActionResult(action.id, false, "no_active_window")

        val node = when (action) {
            is Action.SemanticClick -> find(root, action.target)
            is Action.SemanticScroll -> find(root, action.target)
            else -> null
        }

        val ok = when (action) {
            is Action.SemanticClick -> click(node)
            is Action.SemanticScroll -> scroll(node, action.direction)
            else -> false
        }
        return ActionResult(action.id, ok, if (ok) "executed_semantically" else "semantic_target_not_actionable", System.currentTimeMillis() - start)
    }

    private fun click(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        val parent = node.parent
        if (parent != null && parent.isClickable) return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        val r = Rect()
        node.getBoundsInScreen(r)
        return if (!r.isEmpty) {
            service.execute(Action.Tap("semantic-fallback", r.centerX().toFloat(), r.centerY().toFloat)).success
        } else false
    }

    private fun scroll(node: AccessibilityNodeInfo?, direction: Action.ScrollDirection): Boolean {
        if (node == null) return false
        val action = if (direction == Action.ScrollDirection.FORWARD) {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }
        if (node.isScrollable && node.performAction(action)) return true
        val r = Rect()
        node.getBoundsInScreen(r)
        if (r.isEmpty) return false
        val y1 = if (direction == Action.ScrollDirection.FORWARD) r.bottom - 8f else r.top + 8f
        val y2 = if (direction == Action.ScrollDirection.FORWARD) r.top + 8f else r.bottom - 8f
        return service.execute(Action.Swipe("semantic-scroll-fallback", r.centerX().toFloat(), y1, r.centerX().toFloat(), y2, HumanGestureTiming.scrollMs())).success
    }

    private fun find(root: AccessibilityNodeInfo, target: Action.UiTarget): AccessibilityNodeInfo? {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val node = stack.removeFirst()
            if (matches(node, target)) return node
            for (i in 0 until node.childCount) node.getChild(i)?.let(stack::addLast)
        }
        return null
    }

    private fun matches(node: AccessibilityNodeInfo, target: Action.UiTarget): Boolean {
        fun match(value: CharSequence?, expected: String?): Boolean {
            if (expected == null) return true
            val actual = value?.toString() ?: return false
            return if (target.exact) actual == expected else actual.contains(expected, ignoreCase = true)
        }
        return match(node.text, target.text) &&
            match(node.contentDescription, target.contentDescription) &&
            match(node.viewIdResourceName, target.resourceId) &&
            match(node.className, target.className) &&
            (target.clickable == null || node.isClickable == target.clickable) &&
            (target.scrollable == null || node.isScrollable == target.scrollable)
    }
}
