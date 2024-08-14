package com.melonhead.lib_app_events.events

sealed class AuthenticationEvent: AppEvent {
    data object LoggedIn: AuthenticationEvent()
    data object LoggingIn: AuthenticationEvent()
    data object LoggedOut: AuthenticationEvent()
    data class RefreshToken(val logoutOnFail: Boolean = false): AuthenticationEvent()
}
