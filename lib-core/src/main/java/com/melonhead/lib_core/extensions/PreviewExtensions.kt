package com.melonhead.lib_core.extensions

import com.melonhead.data_shared.models.ui.UIChapter
import com.melonhead.data_shared.models.ui.UIManga
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
        contentRating = "Safe",
        lastChapter = null,
        description = "    Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
    )

    fun previewUIChapters() = listOf(
        UIChapter(
            "",
            "101",
            "Test Title",
            Clock.System.now().epochSeconds,
            true
        ),
        UIChapter(
            "",
            "102",
            "Test Title 2",
            Clock.System.now().epochSeconds,
            false
        )
    )
}
