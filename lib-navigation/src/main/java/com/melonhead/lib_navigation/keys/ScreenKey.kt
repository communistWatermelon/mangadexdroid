package com.melonhead.lib_navigation.keys

sealed class ScreenKey {
    data class LoginScreen(val onLoginTapped: (email: String, password: String) -> Unit) : ScreenKey()
}
