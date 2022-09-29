package com.melonhead.mangadexfollower.models

import kotlinx.datetime.Instant

data class UIChapter(val id: String, val chapter: String?, val title: String?, val createdDate: Instant, val read: Boolean?)
data class UIManga(val id: String, val title: String, val chapters: List<UIChapter>)