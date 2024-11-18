package com.melonhead.lib_navigation.keys

sealed class ScreenKey {
    data class LoginScreen(val onLoginTapped: (email: String, password: String) -> Unit) : ScreenKey()
    data class OauthLoginScreen(
        val onLoginTapped: (
        email: String,
        password: String,
        clientId: String,
        clientPassword: String,
        ) -> Unit,
        val storedEmail: String?,
        val storedClientId: String?,
        val storedClientSecret: String?,
    ) : ScreenKey()
    data class MangaListScreen(
        val buildVersionName: String,
        val buildVersionCode: String,
    ) : ScreenKey()
}
