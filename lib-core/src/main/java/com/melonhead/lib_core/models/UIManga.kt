package com.melonhead.lib_core.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class UIManga(
    val id: String,
    val title: String,
    val chapters: List<UIChapter>,
    val coverFilename: String?,
    val useWebview: Boolean,
    val altTitles: List<String>,
    val tags: List<String>,
    val status: String,
    val contentRating: String,
    val lastChapter: String?,
    val description: String?,
) :
    Parcelable {
    @IgnoredOnParcel
    val coverAddress: String? = if (coverFilename == null) null else "https://mangadex.org/covers/$id/${coverFilename}.256.jpg"
}
