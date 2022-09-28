package com.melonhead.mangadexfollower.models.auth

@kotlinx.serialization.Serializable
data class AuthToken(val session: String, val refresh: String)
@kotlinx.serialization.Serializable
data class AuthRequest(val email: String, val password: String)
@kotlinx.serialization.Serializable
data class RefreshTokenRequest(val token: String)

@kotlinx.serialization.Serializable
data class AuthResponse(val result: String, val token: AuthToken)

@kotlinx.serialization.Serializable
data class CheckTokenResponse(val isAuthenticated: Boolean)
