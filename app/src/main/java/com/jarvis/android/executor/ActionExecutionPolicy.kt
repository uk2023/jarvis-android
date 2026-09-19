package com.jarvis.android.executor

import com.jarvis.android.actions.Action
import com.jarvis.android.gestures.HumanGestureTiming
import kotlin.math.roundToLong
import kotlin.random.Random

/** Deterministic safety limits plus bounded adaptive timing/retry decisions. */
class ActionExecutionPolicy(
    private val maxRetries: Int = 2
) {
    fun attemptsFor(action: Action): Int = when (action) {
        is Action.Tap, is Action.LongPress, is Action.Swipe, is Action.Scroll -> maxRetries + 1
        is Action.SemanticClick, is Action.SemanticScroll -> 2
        is Action.TextInput, is Action.SystemKey -> 1
    }

    fun duration(action: Action): Long = when (action) {
        is Action.Tap -> HumanGestureTiming.tapMs()
        is Action.LongPress -> HumanGestureTiming.longPressMs(action.durationMs)
        is Action.Swipe -> adaptiveSwipe(action.durationMs)
        is Action.Scroll -> HumanGestureTiming.scrollMs()
        else -> 0L
    }

    fun retryable(action: Action): Boolean = when (action) {
        is Action.TextInput, is Action.SystemKey -> false
        else -> true
    }

    private fun adaptiveSwipe(requested: Long): Long {
        val base = requested.coerceIn(180L, 1600L)
        val factor = Random.nextDouble(0.94, 1.10)
        return (base * factor).roundToLong().coerceIn(180L, 1800L)
    }
}
