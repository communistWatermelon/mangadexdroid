package com.melonhead.data_manga.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReadingStatusRequest(
    @SerialName("status") val status: String
) {
    companion object {
        fun from(readingStatus: ReadingStatus): ReadingStatusRequest {
            return ReadingStatusRequest(readingStatus.serialized())
        }
    }
}
