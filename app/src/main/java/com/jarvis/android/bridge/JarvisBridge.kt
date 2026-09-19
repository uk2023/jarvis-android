package com.jarvis.android.bridge

import com.jarvis.android.actions.Action
import com.jarvis.android.actions.ActionResult

interface JarvisBridge {
    fun submit(action: Action): ActionResult
}
