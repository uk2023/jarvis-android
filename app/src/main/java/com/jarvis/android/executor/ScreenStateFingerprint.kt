package com.jarvis.android.executor

import android.view.accessibility.AccessibilityNodeInfo

/** Small accessibility-tree fingerprint used to detect whether a screen changed. */
object ScreenStateFingerprint {
    fun capture(root: AccessibilityNodeInfo?): Long {
        if (root == null) return 0L
        var hash = 1125899906842597L
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.add(root)
        var visited = 0
        while (stack.isNotEmpty() && visited < 600) {
            val node = stack.removeLast()
            hash = mix(hash, node.className?.hashCode() ?: 0)
            hash = mix(hash, node.text?.hashCode() ?: 0)
            hash = mix(hash, node.contentDescription?.hashCode() ?: 0)
            hash = mix(hash, node.isVisibleToUser.hashCode())
            hash = mix(hash, node.isEnabled.hashCode())
            val id = node.viewIdResourceName
            if (id != null) hash = mix(hash, id.hashCode())
            for (i in 0 until node.childCount) node.getChild(i)?.let(stack::addLast)
            visited++
        }
        return hash
    }

    private fun mix(current: Long, value: Int): Long =
        (current xor value.toLong()) * 1099511628211L
}
