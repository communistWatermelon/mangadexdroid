package com.melonhead.data_authentication.models

@kotlinx.serialization.Serializable
internal data class RefreshTokenRequest(val token: String)
