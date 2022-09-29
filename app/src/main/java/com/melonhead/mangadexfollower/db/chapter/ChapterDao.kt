package com.melonhead.mangadexfollower.db.chapter

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapter ORDER BY createdAt desc")
    fun getAll(): Flow<List<ChapterEntity>>

    fun allChapters() = getAll().distinctUntilChanged()

    @Query("SELECT * FROM chapter WHERE manga_id IS :mangaId")
    fun getChaptersForManga(mangaId: String): Flow<List<ChapterEntity>>

    fun chaptersForManga(mangaId: String) = getChaptersForManga(mangaId).distinctUntilChanged()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg chapters: ChapterEntity)

    @Delete
    fun delete(chapters: ChapterEntity)

    @Update
    fun update(vararg chapters: ChapterEntity)
}