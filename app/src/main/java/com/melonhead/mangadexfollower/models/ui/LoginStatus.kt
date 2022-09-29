package com.melonhead.mangadexfollower.models.ui

sealed class LoginStatus {
    object LoggedIn: LoginStatus()
    object LoggedOut: LoginStatus()
    object LoggingIn: LoginStatus()
}