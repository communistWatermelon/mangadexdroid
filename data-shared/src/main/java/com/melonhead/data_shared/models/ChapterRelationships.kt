package com.melonhead.data_shared.models

@kotlinx.serialization.Serializable
data class ChapterRelationships(val id: String, val related: String? = null, val type: String? = null, val attributes: CoverAttributes? = null)
