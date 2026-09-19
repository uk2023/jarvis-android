package com.jarvis.android.gestures

import android.accessibilityservice.GestureDescription
import android.graphics.Path

object GestureBuilder {
    fun line(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long
    ): GestureDescription {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(
            path,
            0L,
            durationMs.coerceAtLeast(1L)
        )
        return GestureDescription.Builder().addStroke(stroke).build()
    }
}
