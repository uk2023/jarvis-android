package com.jarvis.android.semantic

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult
import com.jarvis.android.actions.UiTarget
import com.jarvis.android.accessibility.JarvisAccessibilityService
import com.jarvis.android.gestures.HumanGestureTiming

/** Resolves human-readable UI targets against the live accessibility tree. */
class SemanticUiController(private val service: JarvisAccessibilityService) {
    fun execute(action: Action): ActionResult {
        val start = System.currentTimeMillis()
        val root = service.rootInActiveWindow
            ?: return result(action, start, false, "no_active_window", true, "none")
        val target = when (action) {
            is Action.SemanticClick -> action.target
            is Action.SemanticScroll -> action.target
            else -> return result(action, start, false, "unsupported_semantic_action", false, "none")
        }
        val node = resolve(root, target)
            ?: return result(action, start, false, "semantic_target_not_found", true, "semantic")
        val ok = when (action) {
            is Action.SemanticClick -> click(node)
            is Action.SemanticScroll -> scroll(node, action.direction)
            else -> false
        }
        return result(action, start, ok,
            if (ok) "executed_semantically" else "semantic_target_not_actionable",
            !ok, if (ok) "semantic" else "semantic_failed")
    }

    private fun result(action: Action, start: Long, ok: Boolean, message: String, retryable: Boolean, strategy: String) =
        ActionResult(action.id, ok, message, System.currentTimeMillis() - start, strategy, retryable)

    private fun click(node: AccessibilityNodeInfo): Boolean {
        if (!node.isVisibleToUser || !node.isEnabled) return false
        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        var parent = node.parent
        repeat(4) {
            if (parent == null) return@repeat
            if (parent.isVisibleToUser && parent.isEnabled && parent.isClickable &&
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            parent = parent.parent
        }
        val r = Rect()
        node.getBoundsInScreen(r)
        return if (!r.isEmpty) service.execute(
            Action.Tap("semantic-fallback-${actionSafeId(node)}", r.centerX().toFloat(), r.centerY().toFloat())
        ).success else false
    }

    private fun scroll(node: AccessibilityNodeInfo, direction: Action.ScrollDirection): Boolean {
        if (!node.isVisibleToUser || !node.isEnabled) return false
        val nativeAction = if (direction == Action.ScrollDirection.FORWARD)
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        if (node.isScrollable && node.performAction(nativeAction)) return true

        val r = Rect()
        node.getBoundsInScreen(r)
        if (r.isEmpty || r.width() <= 1 || r.height() <= 1) return false
        val startY = if (direction == Action.ScrollDirection.FORWARD) r.bottom - 8f else r.top + 8f
        val endY = if (direction == Action.ScrollDirection.FORWARD) r.top + 8f else r.bottom - 8f
        if (startY == endY) return false
        return service.execute(Action.Swipe(
            "semantic-scroll-${actionSafeId(node)}",
            r.centerX().toFloat(), startY, r.centerX().toFloat(), endY,
            HumanGestureTiming.scrollMs()
        )).success
    }

    /** Scores candidates instead of trusting the first accessibility node encountered. */
    private fun resolve(root: AccessibilityNodeInfo, target: UiTarget): AccessibilityNodeInfo? {
        val candidates = ArrayList<Pair<AccessibilityNodeInfo, Int>>()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            score(node, target)?.let { candidates += node to it }
            for (i in 0 until node.childCount) node.getChild(i)?.let(queue::addLast)
        }
        return candidates.maxByOrNull { it.second }?.first
    }

    private fun score(node: AccessibilityNodeInfo, target: UiTarget): Int? {
        if (!matchesRequired(node, target) || !node.isVisibleToUser || !node.isEnabled) return null
        var score = 45
        if (target.text != null) score += 30
        if (target.contentDescription != null) score += 30
        if (target.resourceId != null) score += 40
        if (target.className != null) score += 10
        if (target.clickable != null) score += 5
        if (target.scrollable != null) score += 5
        if (node.isFocused) score += 3
        return score
    }

    private fun matchesRequired(node: AccessibilityNodeInfo, target: UiTarget): Boolean {
        fun match(value: CharSequence?, expected: String?): Boolean {
            if (expected == null) return true
            val actual = value?.toString() ?: return false
            return if (target.exact) actual == expected else actual.contains(expected, ignoreCase = true)
        }
        return match(node.text, target.text) && match(node.contentDescription, target.contentDescription) &&
            match(node.viewIdResourceName, target.resourceId) && match(node.className, target.className) &&
            (target.clickable == null || node.isClickable == target.clickable) &&
            (target.scrollable == null || node.isScrollable == target.scrollable)
    }

    private fun actionSafeId(node: AccessibilityNodeInfo): String =
        Integer.toHexString(System.identityHashCode(node))
}
