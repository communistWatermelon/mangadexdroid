package com.melonhead.data_shared.models.ui

sealed class MangaRefreshStatus {
    val text: String
        get() = when (this) {
            Following -> "Fetching Followed Manga..."
            MangaSeries -> "Fetching Series Info..."
            None -> ""
            ReadStatus -> "Fetching Read Status..."
            FetchingChapters -> "Downloading Chapter Images..."
        }
}

data object Following: MangaRefreshStatus()
data object MangaSeries: MangaRefreshStatus()
data object None: MangaRefreshStatus()
data object ReadStatus: MangaRefreshStatus()
data object FetchingChapters: MangaRefreshStatus()
