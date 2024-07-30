 package com.melonhead.data_authentication.models

@kotlinx.serialization.Serializable
data class AuthRequest(val email: String, val password: String)
