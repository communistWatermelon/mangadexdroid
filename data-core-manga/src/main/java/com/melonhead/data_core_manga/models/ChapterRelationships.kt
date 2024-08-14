package com.melonhead.data_core_manga.models

@kotlinx.serialization.Serializable
data class ChapterRelationships(val id: String, val related: String? = null, val type: String? = null, val attributes: CoverAttributes? = null)
