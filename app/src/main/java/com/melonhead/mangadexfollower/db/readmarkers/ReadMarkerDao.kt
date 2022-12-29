package com.melonhead.mangadexfollower.db.readmarkers

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface ReadMarkerDao {
    @Query("SELECT * FROM readmarker ORDER BY createdAt desc")
    fun getAll(): Flow<List<ReadMarkerEntity>>

    fun allMarkers() = getAll().distinctUntilChanged()

    @Query("SELECT * FROM readmarker WHERE manga_id = :mangaId AND chapter = :chapter")
    fun getEntity(mangaId: String, chapter: String?): ReadMarkerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg readMarker: ReadMarkerEntity)

    @Update
    suspend fun update(vararg readMarkers: ReadMarkerEntity)
}