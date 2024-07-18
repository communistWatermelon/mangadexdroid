package com.melonhead.lib_database.readmarkers

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReadMarkerEntity::class], version = 1)
internal abstract class ReadMarkerDatabase: RoomDatabase() {
    abstract fun readMarkersDao(): ReadMarkerDao
}
