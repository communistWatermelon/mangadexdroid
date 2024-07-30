package com.melonhead.data_authentication.models

@kotlinx.serialization.Serializable
data class AuthResponse(val result: String, val token: AuthToken)
