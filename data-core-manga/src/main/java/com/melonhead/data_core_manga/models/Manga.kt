package com.melonhead.data_core_manga.models

@kotlinx.serialization.Serializable
data class Manga(val id: String, val attributes: MangaAttributes, val relationships: List<com.melonhead.data_core_manga.models.ChapterRelationships>) {
    private val coverArtRelationships: com.melonhead.data_core_manga.models.ChapterRelationships? = relationships.firstOrNull { it.type == "cover_art" }
    val fileName: String? = coverArtRelationships?.attributes?.fileName

    companion object
}
