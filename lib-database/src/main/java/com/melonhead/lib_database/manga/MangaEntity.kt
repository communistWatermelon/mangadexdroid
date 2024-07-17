package com.melonhead.lib_database.manga

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.melonhead.lib_database.ListConverter

@Entity(tableName = "manga")
@TypeConverters(ListConverter::class)
data class MangaEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "manga_titles") val mangaTitles: List<String>,
    @ColumnInfo(name = "manga_cover_id") val mangaCoverId: String? = null,
    @ColumnInfo(name = "use_webview") val useWebview: Boolean = false,
    @ColumnInfo(name = "chosen_title") val chosenTitle: String? = null,
) {
    // required for mapping functions
    companion object
}
