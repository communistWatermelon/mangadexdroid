package com.melonhead.data_authentication.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthToken(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String
)
