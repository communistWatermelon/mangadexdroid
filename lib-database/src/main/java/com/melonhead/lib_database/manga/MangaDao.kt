package com.melonhead.lib_database.manga

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga")
    fun getAll(): Flow<List<MangaEntity>>

    @Query("SELECT * from manga")
    suspend fun getAllSync(): List<MangaEntity>

    fun allSeries() = getAll().distinctUntilChanged()

    @Query("SELECT * FROM manga WHERE id IS :mangaId")
    fun getMangaById(mangaId: String): Flow<MangaEntity?>

    fun mangaById(mangaId: String) = getMangaById(mangaId).distinctUntilChanged()

    @Query("SELECT EXISTS(SELECT * FROM manga WHERE id = :mangaId)")
    suspend fun containsManga(mangaId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg chapters: MangaEntity)

    @Delete
    suspend fun delete(chapters: MangaEntity)

    @Update
    suspend fun update(manga: MangaEntity)
}
