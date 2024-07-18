package com.melonhead.lib_database.manga

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.melonhead.lib_database.ListConverter
import com.melonhead.lib_database.MangaTagConverter

@Entity(tableName = "manga")
@TypeConverters(ListConverter::class, MangaTagConverter::class)
data class MangaEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "manga_titles") val mangaTitles: List<String>,
    @ColumnInfo(name = "manga_cover_id") val mangaCoverId: String? = null,
    @ColumnInfo(name = "use_webview") val useWebview: Boolean = false,
    @ColumnInfo(name = "chosen_title") val chosenTitle: String? = null,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "tags") val tags: List<MangaTag>,
    @ColumnInfo(name = "content_rating") val contentRating: String,
    @ColumnInfo(name = "last_chapter") val lastChapter: String?,
    @ColumnInfo(name = "description") val description: String?,
) {
    // required for mapping functions
    companion object
}
