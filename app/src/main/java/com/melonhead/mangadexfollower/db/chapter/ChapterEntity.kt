package com.melonhead.mangadexfollower.db.chapter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.melonhead.mangadexfollower.db.InstantConverter
import com.melonhead.mangadexfollower.models.content.Chapter
import kotlinx.datetime.Instant

@Entity(tableName = "chapter")
@TypeConverters(InstantConverter::class)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "manga_id") val mangaId: String,
    @ColumnInfo(name = "chapter_title") val chapterTitle: String?,
    @ColumnInfo(name = "chapter") val chapter: String?,
    @ColumnInfo(name = "createdAt") val createdAt: Instant,
) {
    companion object {
        fun from(chapter: Chapter): ChapterEntity {
            return ChapterEntity(
                id = chapter.id,
                mangaId = chapter.relationships?.firstOrNull { it.type == "manga" }!!.id,
                chapterTitle = chapter.attributes.title,
                chapter = chapter.attributes.chapter,
                createdAt = chapter.attributes.createdAt,
            )
        }
    }
}