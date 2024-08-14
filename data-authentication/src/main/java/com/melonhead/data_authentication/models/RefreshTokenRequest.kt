package com.melonhead.data_authentication.models

@kotlinx.serialization.Serializable
data class RefreshTokenRequest(val token: String)
