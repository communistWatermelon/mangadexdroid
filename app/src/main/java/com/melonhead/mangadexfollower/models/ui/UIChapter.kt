package com.melonhead.mangadexfollower.models.ui

import android.os.Parcelable
import kotlinx.datetime.Instant
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UIChapter(val id: String, val chapter: String?, val title: String?, val createdDate: Long, val read: Boolean?) :
    Parcelable {
    @IgnoredOnParcel
    val webAddress: String = "https://mangadex.org/chapter/$id"
}