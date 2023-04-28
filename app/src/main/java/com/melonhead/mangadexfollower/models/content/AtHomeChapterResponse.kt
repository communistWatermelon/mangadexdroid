package com.melonhead.mangadexfollower.models.content

data class AtHomeChapterDataResponse(
    val data: List<String>,
    val dataSaver: List<String>,
)

data class AtHomeChapterResponse(
    val baseUrl: String,
    val chapter: AtHomeChapterDataResponse
) {
    fun pages(): List<String> {
        return chapter.data.map { baseUrl + it }
    }

    fun pagesDataSaver(): List<String> {
        return chapter.data.map { baseUrl + it }
    }
}