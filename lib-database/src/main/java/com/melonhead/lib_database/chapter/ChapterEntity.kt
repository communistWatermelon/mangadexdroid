package com.melonhead.lib_database.chapter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.melonhead.lib_database.InstantConverter
import kotlinx.datetime.Instant

@Entity(tableName = "chapter")
@TypeConverters(InstantConverter::class)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    @ColumnInfo(name = "manga_id") val mangaId: String,
    @ColumnInfo(name = "chapter_title") val chapterTitle: String?,
    @ColumnInfo(name = "chapter") val chapter: String?,
    @ColumnInfo(name = "createdAt") val createdAt: Instant,
    @ColumnInfo(name = "externalUrl") val externalUrl: String?,
) {
    // required for mapping functions
    companion object
}
