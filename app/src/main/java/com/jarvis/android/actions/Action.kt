package com.jarvis.android.actions

sealed interface Action {
    val id: String

    data class Tap(
        override val id: String,
        val x: Float,
        val y: Float
    ) : Action

    data class LongPress(
        override val id: String,
        val x: Float,
        val y: Float,
        val durationMs: Long = 550L
    ) : Action

    data class Swipe(
        override val id: String,
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val durationMs: Long = 350L
    ) : Action

    data class Scroll(
        override val id: String,
        val x: Float,
        val y: Float,
        val deltaX: Float,
        val deltaY: Float
    ) : Action

    data class TextInput(
        override val id: String,
        val text: String
    ) : Action

    data class SystemKey(
        override val id: String,
        val key: Key
    ) : Action

    enum class Key { BACK, HOME, RECENTS }
}
