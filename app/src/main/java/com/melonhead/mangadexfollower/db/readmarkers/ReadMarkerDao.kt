package com.melonhead.mangadexfollower.db.readmarkers

import androidx.room.*

@Dao
interface ReadMarkerDao {
    @Query("SELECT EXISTS(SELECT * FROM readmarker WHERE manga_id = :mangaId AND chapter = :chapter)")
    suspend fun isRead(mangaId: String, chapter: String?): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg readMarker: ReadMarkerEntity)

    @Update
    suspend fun update(vararg readMarkers: ReadMarkerEntity)
}