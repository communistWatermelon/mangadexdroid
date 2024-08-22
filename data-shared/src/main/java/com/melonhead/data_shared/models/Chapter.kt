package com.melonhead.data_shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(val id: String, val attributes: ChapterAttributes, val relationships: List<ChapterRelationships>?)
