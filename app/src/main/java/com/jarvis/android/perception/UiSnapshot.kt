package com.jarvis.android.perception

/** Immutable screen state exposed to JARVIS cognition without leaking AccessibilityNodeInfo. */
data class UiSnapshot(
    val packageName: String?,
    val className: String?,
    val windowTitle: String?,
    val nodes: List<UiNode>
)

data class UiNode(
    val index: Int,
    val parentIndex: Int?,
    val text: String?,
    val contentDescription: String?,
    val resourceId: String?,
    val className: String?,
    val boundsLeft: Int,
    val boundsTop: Int,
    val boundsRight: Int,
    val boundsBottom: Int,
    val visible: Boolean,
    val enabled: Boolean,
    val clickable: Boolean,
    val scrollable: Boolean,
    val editable: Boolean,
    val focused: Boolean,
    val checkable: Boolean,
    val checked: Boolean
)
