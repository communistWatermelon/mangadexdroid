package com.melonhead.lib_database

import androidx.room.TypeConverter
import com.melonhead.lib_database.manga.MangaTag

object MangaTagConverter {
    @TypeConverter
    fun toMangaTag(string: String): MangaTag {
        val parts = string.split(".")
        return MangaTag(parts[0], parts[1])
    }

    @TypeConverter
    fun fromMangaTag(mangaTag: MangaTag): String {
        return "${mangaTag.id}.${mangaTag.name}"
    }

    @TypeConverter
    fun toMangaTagList(string: String): List<MangaTag> {
        return string.split(",").map { toMangaTag(it) }
    }

    @TypeConverter
    fun fromMangaTagList(mangaTags: List<MangaTag>): String {
        return mangaTags.joinToString(",") { fromMangaTag(it) }
    }
}
