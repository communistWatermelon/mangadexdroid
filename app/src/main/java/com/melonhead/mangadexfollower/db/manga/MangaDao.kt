package com.melonhead.mangadexfollower.db.manga

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga")
    fun getAll(): Flow<List<MangaEntity>>

    fun allSeries() = getAll().distinctUntilChanged()

    @Query("SELECT * FROM manga WHERE id IS :mangaId")
    fun getMangaById(mangaId: String): Flow<MangaEntity?>

    fun mangaById(mangaId: String) = getMangaById(mangaId).distinctUntilChanged()

    @Query("SELECT EXISTS(SELECT * FROM manga WHERE id = :mangaId)")
    fun containsManga(mangaId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg chapters: MangaEntity)

    @Delete
    fun delete(chapters: MangaEntity)

    @Update
    fun update(chapters: MangaEntity)
}