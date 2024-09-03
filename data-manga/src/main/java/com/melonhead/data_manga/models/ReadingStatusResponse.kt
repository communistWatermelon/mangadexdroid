package com.melonhead.data_manga.models

import kotlinx.serialization.Serializable

@Serializable
data class ReadingStatusResponse(
    val status: String,
)
