package com.jarvis.android.executor

import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult

interface ActionExecutor {
    fun execute(action: Action): ActionResult
}
