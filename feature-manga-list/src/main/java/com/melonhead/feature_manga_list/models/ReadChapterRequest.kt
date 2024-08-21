package com.melonhead.feature_manga_list.models

import com.melonhead.core_ui.models.UIChapter

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
