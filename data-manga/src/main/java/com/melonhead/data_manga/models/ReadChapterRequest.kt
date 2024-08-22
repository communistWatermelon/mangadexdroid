package com.melonhead.data_manga.models

import com.melonhead.lib_core.models.UIChapter

@kotlinx.serialization.Serializable
internal data class ReadChapterRequest(
    val chapterIdsRead: List<String> = listOf(),
    val chapterIdsUnread: List<String> = listOf(),
) {
    companion object {
        fun from(chapter: UIChapter, readStatus: Boolean): ReadChapterRequest {
            return ReadChapterRequest(
                chapterIdsRead = if (readStatus) listOf(chapter.id) else emptyList(),
                chapterIdsUnread = if (!readStatus) listOf(chapter.id) else emptyList(),
            )
        }
    }
}
