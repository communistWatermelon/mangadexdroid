package com.melonhead.mangadexfollower.db.readmarkers

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReadMarkerEntity::class], version = 1)
abstract class ReadMarkerDatabase: RoomDatabase() {
    abstract fun readMarkersDao(): ReadMarkerDao
}