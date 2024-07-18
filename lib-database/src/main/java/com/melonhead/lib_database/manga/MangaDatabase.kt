package com.melonhead.lib_database.manga

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MangaEntity::class],
    version = 6
)
internal abstract class MangaDatabase: RoomDatabase() {
    abstract fun mangaDao(): MangaDao
}
