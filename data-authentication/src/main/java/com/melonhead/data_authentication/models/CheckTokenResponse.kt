package com.melonhead.data_authentication.models

@kotlinx.serialization.Serializable
data class CheckTokenResponse(val isAuthenticated: Boolean)
