package com.melonhead.mangadexfollower.models.content

@kotlinx.serialization.Serializable
data class ChapterRelationships(val id: String, val related: String? = null, val type: String? = null, val attributes: CoverAttributes? = null)