package com.jarvis.android.gestures

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import kotlin.math.max
import kotlin.math.min

/** Builds smooth, bounded gesture paths instead of single-segment robotic motion. */
object GestureBuilder {
    fun line(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long
    ): GestureDescription = interpolatedLine(startX, startY, endX, endY, durationMs)

    fun interpolatedLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long,
        samples: Int = 18
    ): GestureDescription {
        val count = max(4, min(samples, 64))
        val path = Path().apply {
            moveTo(startX, startY)
            for (i in 1..count) {
                val t = i.toFloat() / count.toFloat()
                val eased = smoothStep(t)
                val x = startX + (endX - startX) * eased
                val y = startY + (endY - startY) * eased
                lineTo(x, y)
            }
        }
        val stroke = GestureDescription.StrokeDescription(
            path,
            0L,
            durationMs.coerceIn(1L, 10_000L)
        )
        return GestureDescription.Builder().addStroke(stroke).build()
    }

    private fun smoothStep(t: Float): Float = t * t * (3f - 2f * t)
}
