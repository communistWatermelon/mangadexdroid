package com.melonhead.data_authentication.models

import kotlinx.serialization.Serializable

@Serializable
internal data class AuthRequest(val email: String, val password: String)
