package com.jarvis.android.perception

data class UiSnapshot(
    val packageName: String?,
    val className: String?,
    val text: String?,
    val contentDescription: String?,
    val clickable: Boolean,
    val scrollable: Boolean,
    val boundsLeft: Int,
    val boundsTop: Int,
    val boundsRight: Int,
    val boundsBottom: Int
)
