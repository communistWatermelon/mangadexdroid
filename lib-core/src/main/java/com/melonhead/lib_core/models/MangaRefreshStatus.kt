package com.melonhead.lib_core.models

sealed class MangaRefreshStatus {
    val text: String
        get() = when (this) {
            Following -> "Fetching Followed Manga..."
            MangaSeries -> "Fetching Series Info..."
            None -> ""
            ReadStatus -> "Fetching Read Status..."
        }
}

data object Following: MangaRefreshStatus()
data object MangaSeries: MangaRefreshStatus()
data object None: MangaRefreshStatus()
data object ReadStatus: MangaRefreshStatus()
