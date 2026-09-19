package com.jarvis.android.perception

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/** Converts the live accessibility tree into a bounded, serializable UI snapshot. */
class AccessibilitySnapshotter(
    private val maxNodes: Int = 1000
) {
    init {
        require(maxNodes > 0) { "maxNodes must be positive" }
    }

    fun capture(root: AccessibilityNodeInfo?): UiSnapshot? {
        if (root == null) return null

        val nodes = ArrayList<UiNode>(minOf(maxNodes, 128))
        val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int?>>()
        queue.add(root to null)
        var truncated = false

        while (queue.isNotEmpty()) {
            if (nodes.size >= maxNodes) {
                truncated = true
                break
            }

            val (node, parentIndex) = queue.removeFirst()
            val index = nodes.size
            val bounds = Rect()
            node.getBoundsInScreen(bounds)

            nodes += UiNode(
                index = index,
                parentIndex = parentIndex,
                text = node.text?.toString(),
                contentDescription = node.contentDescription?.toString(),
                resourceId = node.viewIdResourceName,
                className = node.className?.toString(),
                boundsLeft = bounds.left,
                boundsTop = bounds.top,
                boundsRight = bounds.right,
                boundsBottom = bounds.bottom,
                visible = node.isVisibleToUser,
                enabled = node.isEnabled,
                clickable = node.isClickable,
                scrollable = node.isScrollable,
                editable = node.isEditable,
                focused = node.isFocused,
                checkable = node.isCheckable,
                checked = node.isChecked
            )

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.addLast(it to index) }
            }
        }

        return UiSnapshot(
            packageName = root.packageName?.toString(),
            className = root.className?.toString(),
            windowTitle = root.window?.title?.toString(),
            nodes = nodes,
            truncated = truncated
        )
    }
}
