package com.melonhead.lib_navigation.keys

sealed class ScreenKey {
    data class LoginScreen(val onLoginTapped: (email: String, password: String) -> Unit) : ScreenKey()
    data class MangaListScreen(
        val buildVersionName: String,
        val buildVersionCode: String,
    ) : ScreenKey()
}
