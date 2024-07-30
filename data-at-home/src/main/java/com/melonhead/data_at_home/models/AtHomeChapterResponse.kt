package com.melonhead.data_at_home.models

@kotlinx.serialization.Serializable
data class AtHomeChapterDataResponse(
    val hash: String,
    val data: List<String>,
    val dataSaver: List<String>,
)

@kotlinx.serialization.Serializable
data class AtHomeChapterResponse(
    val baseUrl: String,
    val chapter: AtHomeChapterDataResponse
) {
    fun pages(): List<String> {
        return chapter.data.map { "$baseUrl/data/${chapter.hash}/$it" }
    }

    fun pagesDataSaver(): List<String> {
        return chapter.dataSaver.map { "$baseUrl/data-saver/${chapter.hash}/$it" }
    }
}
