package com.melonhead.lib_app_context

interface AppContext {
    var isInForeground: Boolean
}

internal class AppContextImpl: AppContext {
    override var isInForeground: Boolean = false
}
