package com.melonhead.mangadexfollower.models.content

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray

@kotlinx.serialization.Serializable
data class MangaTagAttributes(
    val name: Map<String, String>,
)

@kotlinx.serialization.Serializable
data class MangaTags(
    val id: String,
    val attributes: MangaTagAttributes,
)

@kotlinx.serialization.Serializable
data class MangaAttributes(
    val title: Map<String, String>,
    val altTitles: JsonElement?,
    val status: String,
    val tags: List<MangaTags>,
    val contentRating: String,
    val lastChapter: String?,
) {
    fun getEnglishTitles(): List<String> {
        val englishTitles = mutableListOf<String>()
        for ((key, value) in title) {
            if (key == "en") {
                englishTitles.add(value)
            }
        }
        val altArray = altTitles?.jsonArray
        if (altArray != null) {
            englishTitles.addAll(altArray.filterIsInstance<JsonObject>().mapNotNull { it.getOrDefault("en", null) }.filterIsInstance<JsonPrimitive>().map { it.content })
        }
        return englishTitles
    }
}
