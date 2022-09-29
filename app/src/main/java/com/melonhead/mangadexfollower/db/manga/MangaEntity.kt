package com.melonhead.mangadexfollower.db.manga

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.melonhead.mangadexfollower.models.content.Manga


@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "manga_title") val mangaTitle: String?,
) {
    companion object {
        fun from(manga: Manga): MangaEntity {
            return MangaEntity(
                id = manga.id,
                mangaTitle = manga.attributes.title.values.first()
            )
        }
    }
}