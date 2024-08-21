package com.melonhead.data_authentication.models

@kotlinx.serialization.Serializable
internal data class AuthResponse(val result: String, val token: AuthToken)
