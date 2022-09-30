package com.melonhead.mangadexfollower.models.cover

@kotlinx.serialization.Serializable
data class Cover(val id: String, val attributes: CoverAttributes, val relationships: List<CoverRelationships>) {
    val mangaId: String? = relationships.firstOrNull { it.type == "manga" }?.id
    val fileName: String = attributes.fileName
}