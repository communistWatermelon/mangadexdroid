package com.melonhead.mangadexfollower.models.auth

@kotlinx.serialization.Serializable
data class CheckTokenResponse(val isAuthenticated: Boolean)