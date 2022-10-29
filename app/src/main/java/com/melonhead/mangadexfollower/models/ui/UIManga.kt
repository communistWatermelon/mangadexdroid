package com.melonhead.mangadexfollower.models.ui

data class UIManga(val id: String, val title: String, val chapters: List<UIChapter>, val coverFilename: String?) {
    val coverAddress: String? = if (coverFilename == null) null else "https://mangadex.org/covers/$id/${coverFilename}.256.jpg"
}