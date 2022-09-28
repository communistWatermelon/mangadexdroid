package com.melonhead.mangadexfollower.models.auth

@kotlinx.serialization.Serializable
data class ChapterAttributes(val title: String?, val chapter: String?)

@kotlinx.serialization.Serializable
data class Chapter(val id: String, val attributes: ChapterAttributes)

