package com.melonhead.lib_database.chapter

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapter ORDER BY createdAt desc")
    fun getAll(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapter ORDER BY createdAt desc")
    suspend fun getAllSync(): List<ChapterEntity>

    fun allChapters() = getAll().distinctUntilChanged()

    @Query("SELECT * FROM chapter WHERE manga_id IS :mangaId")
    fun getChaptersForManga(mangaId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapter WHERE id IS :chapterId")
    suspend fun getChapterForId(chapterId: String): ChapterEntity

    fun chaptersForManga(mangaId: String) = getChaptersForManga(mangaId).distinctUntilChanged()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg chapters: ChapterEntity)

    @Query("SELECT EXISTS(SELECT * FROM chapter WHERE id = :chapterId)")
    suspend fun containsChapter(chapterId: String): Boolean

    @Delete
    suspend fun delete(chapters: ChapterEntity)

    @Update
    suspend fun update(vararg chapters: ChapterEntity)
}
