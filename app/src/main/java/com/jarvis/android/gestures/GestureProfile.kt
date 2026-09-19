package com.jarvis.android.gestures

data class GestureProfile(
    val durationScale: Float = 1.0f,
    val minimumDurationMs: Long = 180L,
    val jitterPx: Float = 0f
)
