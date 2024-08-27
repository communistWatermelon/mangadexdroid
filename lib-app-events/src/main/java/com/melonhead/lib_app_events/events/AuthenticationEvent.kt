package com.melonhead.lib_app_events.events

import java.util.concurrent.CompletableFuture

sealed class AuthenticationEvent: AppEvent {
    data object LoggedIn: AuthenticationEvent()
    data object LoggingIn: AuthenticationEvent()
    data object LoggedOut: AuthenticationEvent()
    data class RefreshToken(val logoutOnFail: Boolean = false, val completionJob: CompletableFuture<Unit>? = null): AuthenticationEvent()
}
