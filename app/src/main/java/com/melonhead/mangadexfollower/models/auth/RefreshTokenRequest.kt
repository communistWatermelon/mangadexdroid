package com.melonhead.mangadexfollower.models.auth

@kotlinx.serialization.Serializable
data class RefreshTokenRequest(val token: String)