package com.melonhead.mangadexfollower.extensions

import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import kotlinx.datetime.Clock

object Previews {
    fun previewUIManga(
        chapters: List<UIChapter> = previewUIChapters()
    ) = UIManga(
        "",
        "Test Manga",
        chapters,
        null,
        false,
        altTitles = listOf("Test Manga"),
        tags = listOf("Comedy", "Slice of Life", "Romance"),
        status = "ongoing",
        contentRating = "Safe"
    )

    fun previewUIChapters() = listOf(
        UIChapter("", "101", "Test Title", Clock.System.now().epochSeconds, true),
        UIChapter("", "102", "Test Title 2", Clock.System.now().epochSeconds, false)
    )
}
