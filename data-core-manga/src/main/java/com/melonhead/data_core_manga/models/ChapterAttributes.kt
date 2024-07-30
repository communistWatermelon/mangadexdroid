package com.melonhead.data_core_manga.models

import kotlinx.datetime.Instant

@kotlinx.serialization.Serializable
data class ChapterAttributes(val title: String?, val chapter: String?, val createdAt: Instant, val externalUrl: String?)
