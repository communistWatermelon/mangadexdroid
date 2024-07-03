package com.melonhead.mangadexfollower.db.manga

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.melonhead.mangadexfollower.db.InstantConverter
import com.melonhead.mangadexfollower.db.ListConverter
import com.melonhead.mangadexfollower.models.content.Manga

@Entity(tableName = "manga")
@TypeConverters(ListConverter::class)
data class MangaEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "manga_titles") val mangaTitles: List<String>,
    @ColumnInfo(name = "manga_cover_id") val mangaCoverId: String? = null,
    @ColumnInfo(name = "use_webview") val useWebview: Boolean = false,
    @ColumnInfo(name = "chosen_title") val chosenTitle: String? = null,
) {
    companion object {
        fun from(manga: Manga): MangaEntity {
            val titles = manga.attributes.getEnglishTitles()
            return MangaEntity(
                id = manga.id,
                mangaTitles = titles,
                chosenTitle = titles.first(),
                mangaCoverId = manga.fileName
            )
        }
    }
}