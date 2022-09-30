 package com.melonhead.mangadexfollower.models.auth

@kotlinx.serialization.Serializable
data class AuthRequest(val email: String, val password: String)