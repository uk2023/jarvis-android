package com.jarvis.android.safety

import android.graphics.Rect
import com.jarvis.android.actions.Action

class ActionValidator {
    fun validate(action: Action, bounds: Rect): Result<Unit> = when (action) {
        is Action.Tap -> coordinate(action.x, action.y, bounds)
        is Action.LongPress -> coordinate(action.x, action.y, bounds)
        is Action.Swipe -> if (inside(action.startX, action.startY, bounds) &&
            inside(action.endX, action.endY, bounds)) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Swipe coordinates outside screen bounds"))
        is Action.Scroll -> coordinate(action.x, action.y, bounds)
        is Action.TextInput -> if (action.text.length <= 10000) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Text input exceeds safety limit"))
        is Action.SystemKey -> Result.success(Unit)
    }

    private fun coordinate(x: Float, y: Float, bounds: Rect): Result<Unit> =
        if (inside(x, y, bounds)) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Coordinate outside screen bounds"))

    private fun inside(x: Float, y: Float, bounds: Rect): Boolean =
        x >= bounds.left && x < bounds.right && y >= bounds.top && y < bounds.bottom
}
