package com.melonhead.lib_database.di

import androidx.room.Room
import com.melonhead.lib_database.chapter.ChapterDatabase
import com.melonhead.lib_database.manga.MangaDatabase
import com.melonhead.lib_database.readmarkers.ReadMarkerDatabase
import org.koin.dsl.module

val DBModule = module {
    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            ChapterDatabase::class.java, "chapter"
        ).fallbackToDestructiveMigration().build()
    }

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            MangaDatabase::class.java, "manga"
        ).fallbackToDestructiveMigration().build()
    }

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            ReadMarkerDatabase::class.java, "readmarker"
        ).fallbackToDestructiveMigration().build()
    }

    single {
        get<MangaDatabase>().mangaDao()
    }

    single {
        get<ChapterDatabase>().chapterDao()
    }

    single {
        get<ReadMarkerDatabase>().readMarkersDao()
    }
}
