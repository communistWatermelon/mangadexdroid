package com.melonhead.data_authentication.models

@kotlinx.serialization.Serializable
data class AuthToken(val session: String, val refresh: String)
