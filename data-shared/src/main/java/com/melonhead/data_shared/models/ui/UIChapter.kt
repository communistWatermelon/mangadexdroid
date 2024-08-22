package com.melonhead.data_shared.models.ui

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class UIChapter(
    val id: String,
    val chapter: String?,
    val title: String?,
    val createdDate: Long,
    val read: Boolean,
    val externalUrl: String? = null,
    val cachedPages: Int? = null,
) :
    Parcelable {
    @IgnoredOnParcel
    val webAddress: String = "https://mangadex.org/chapter/$id"
}
