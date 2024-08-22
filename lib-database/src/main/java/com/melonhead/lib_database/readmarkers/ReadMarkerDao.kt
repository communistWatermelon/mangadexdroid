package com.melonhead.lib_database.readmarkers

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface ReadMarkerDao {
    @Query("SELECT * FROM readmarker ORDER BY createdAt desc")
    fun getAll(): Flow<List<ReadMarkerEntity>>

    fun allMarkers() = getAll().distinctUntilChanged()

    @Query("SELECT * FROM readmarker WHERE manga_id = :mangaId AND chapter = :chapter")
    fun getEntityByChapter(mangaId: String, chapter: String?): ReadMarkerEntity?

    @Query("SELECT * FROM readmarker WHERE manga_id = :mangaId AND id = :chapterId")
    fun getEntityById(mangaId: String, chapterId: String): ReadMarkerEntity?

    @Query("SELECT read_status FROM readmarker WHERE manga_id = :mangaId AND chapter = :chapter")
    fun isRead(mangaId: String, chapter: String?): Boolean?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg readMarker: ReadMarkerEntity)

    @Update
    suspend fun update(vararg readMarkers: ReadMarkerEntity)
}
