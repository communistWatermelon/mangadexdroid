package com.melonhead.mangadexfollower.models.content

import kotlinx.datetime.Instant

@kotlinx.serialization.Serializable
data class ChapterAttributes(val title: String?, val chapter: String?, val createdAt: Instant)