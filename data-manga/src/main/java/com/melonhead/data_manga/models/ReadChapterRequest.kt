package com.melonhead.data_manga.models

@kotlinx.serialization.Serializable
internal data class ReadChapterRequest(
    val chapterIdsRead: List<String> = listOf(),
    val chapterIdsUnread: List<String> = listOf(),
) {
    companion object {
        fun from(chapterId: String, readStatus: Boolean): ReadChapterRequest {
            return ReadChapterRequest(
                chapterIdsRead = if (readStatus) listOf(chapterId) else emptyList(),
                chapterIdsUnread = if (!readStatus) listOf(chapterId) else emptyList(),
            )
        }
    }
}
