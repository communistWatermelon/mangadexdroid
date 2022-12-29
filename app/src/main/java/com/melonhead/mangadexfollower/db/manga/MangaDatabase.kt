package com.melonhead.mangadexfollower.db.manga

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MangaEntity::class], version = 2)
abstract class MangaDatabase: RoomDatabase() {
    abstract fun mangaDao(): MangaDao
}