package com.jarvis.android.gestures

import kotlin.math.roundToLong
import kotlin.random.Random

/** Timing model only; it does not decide what JARVIS should do. */
object HumanGestureTiming {
    fun tapMs(): Long = jitter(65L, 35L, 140L)
    fun longPressMs(requested: Long): Long = requested.coerceIn(450L, 1800L) + jitter(0L, 35L, 90L)
    fun swipeMs(requested: Long): Long = (requested.coerceIn(180L, 1600L) * jitterFactor()).roundToLong()
    fun scrollMs(): Long = jitter(220L, 50L, 420L)

    private fun jitter(base: Long, spread: Long, min: Long): Long = (base + Random.nextLong(-spread, spread + 1)).coerceAtLeast(min)
    private fun jitterFactor(): Double = Random.nextDouble(0.90, 1.12)
}
