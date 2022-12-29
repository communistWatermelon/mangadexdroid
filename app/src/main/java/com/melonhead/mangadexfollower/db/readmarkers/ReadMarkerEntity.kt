package com.melonhead.mangadexfollower.db.readmarkers

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.melonhead.mangadexfollower.db.InstantConverter
import com.melonhead.mangadexfollower.db.chapter.ChapterEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(tableName = "readmarker")
@TypeConverters(InstantConverter::class)
data class ReadMarkerEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "manga_id") val mangaId: String,
    @ColumnInfo(name = "chapter") val chapter: String?,
    @ColumnInfo(name = "read_status") val readStatus: Boolean?,
    @ColumnInfo(name = "createdAt") val createdAt: Instant,
) {
    companion object {
        fun from(chapter: ChapterEntity, read: Boolean?): ReadMarkerEntity {
            return ReadMarkerEntity(
                id = "${chapter.mangaId}_${chapter.chapter}",
                mangaId = chapter.mangaId,
                chapter = chapter.chapter,
                createdAt = Clock.System.now(),
                readStatus = read
            )
        }
    }
}