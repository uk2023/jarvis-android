package com.jarvis.android.actions

data class ActionResult(
    val actionId: String,
    val success: Boolean,
    val message: String,
    val durationMs: Long = 0L
)
