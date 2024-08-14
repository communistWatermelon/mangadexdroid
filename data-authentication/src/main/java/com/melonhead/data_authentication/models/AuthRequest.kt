package com.melonhead.data_authentication.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val email: String, val password: String)
