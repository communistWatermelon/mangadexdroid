package com.melonhead.mangadexfollower.models.content

import kotlinx.datetime.Instant

@kotlinx.serialization.Serializable
data class ChapterAttributes(val title: String?, val chapter: String?, val readableAt: Instant?)

@kotlinx.serialization.Serializable
data class ChapterRelationships(val id: String, val related: String? = null, val type: String? = null)

@kotlinx.serialization.Serializable
data class Chapter(val id: String, val attributes: ChapterAttributes, val relationships: List<ChapterRelationships>?)

