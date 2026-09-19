package com.jarvis.android.actions

/** Structured outcome returned to the existing JARVIS runtime. */
data class ActionResult(
    val actionId: String,
    val success: Boolean,
    val message: String,
    val durationMs: Long = 0L,
    val strategy: String? = null,
    val retryable: Boolean = false
)
