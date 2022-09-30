package com.melonhead.mangadexfollower.models.content

@kotlinx.serialization.Serializable
data class Chapter(val id: String, val attributes: ChapterAttributes, val relationships: List<ChapterRelationships>?)

