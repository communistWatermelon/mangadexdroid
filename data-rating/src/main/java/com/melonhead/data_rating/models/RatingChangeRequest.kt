package com.melonhead.data_rating.models

import kotlinx.serialization.Serializable

@Serializable
internal data class RatingChangeRequest(
    val rating: Int
)
