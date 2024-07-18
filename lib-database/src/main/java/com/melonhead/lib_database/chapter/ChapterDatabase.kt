package com.melonhead.lib_database.chapter

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChapterEntity::class], version = 2)
internal abstract class ChapterDatabase: RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
}
