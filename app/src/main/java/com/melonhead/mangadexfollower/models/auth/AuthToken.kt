package com.melonhead.mangadexfollower.models.auth

@kotlinx.serialization.Serializable
data class AuthToken(val session: String, val refresh: String)