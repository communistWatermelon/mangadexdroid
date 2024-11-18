package com.melonhead.data_authentication.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OAuthRefreshResponse(
    @SerialName("access_token")
    val accessToken: String,
)
