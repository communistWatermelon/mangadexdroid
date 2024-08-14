package com.melonhead.data_core_manga.models

@kotlinx.serialization.Serializable
data class Chapter(val id: String, val attributes: ChapterAttributes, val relationships: List<ChapterRelationships>?)
