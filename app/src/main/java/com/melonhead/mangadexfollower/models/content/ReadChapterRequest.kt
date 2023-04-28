package com.melonhead.mangadexfollower.models.content

import com.melonhead.mangadexfollower.models.ui.UIChapter

@kotlinx.serialization.Serializable
data class ReadChapterRequest(
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