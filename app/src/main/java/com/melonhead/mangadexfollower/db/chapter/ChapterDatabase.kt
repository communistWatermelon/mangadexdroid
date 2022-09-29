package com.melonhead.mangadexfollower.db.chapter

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChapterEntity::class], version = 1)
abstract class ChapterDatabase: RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
}