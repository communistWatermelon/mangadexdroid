package com.melonhead.feature_authentication.models

sealed class LoginStatus {
    object LoggedIn: LoginStatus()
    object LoggedOut: LoginStatus()
    object LoggingIn: LoginStatus()
}
