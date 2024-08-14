package com.melonhead.data_manga.models

@kotlinx.serialization.Serializable
internal data class ReadChapterRequest(
    val chapterIdsRead: List<String> = listOf(),
    val chapterIdsUnread: List<String> = listOf(),
) {
    companion object {
        fun from(chapter: com.melonhead.data_core_manga_ui.UIChapter, readStatus: Boolean): ReadChapterRequest {
            return ReadChapterRequest(
                chapterIdsRead = if (readStatus) listOf(chapter.id) else emptyList(),
                chapterIdsUnread = if (!readStatus) listOf(chapter.id) else emptyList(),
            )
        }
    }
}
