package com.melonhead.lib_database.di

import androidx.room.Room
import com.melonhead.lib_database.chapter.ChapterDatabase
import com.melonhead.lib_database.manga.MangaDBMigrations
import com.melonhead.lib_database.manga.MangaDatabase
import com.melonhead.lib_database.readmarkers.ReadMarkerDatabase
import org.koin.dsl.module

val DBModule = module {
    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            ChapterDatabase::class.java, "chapter"
        ).build()
    }

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            MangaDatabase::class.java, "manga"
        ).addMigrations(
            MangaDBMigrations.MIGRATION_1_2,
            MangaDBMigrations.MIGRATION_2_3,
            MangaDBMigrations.MIGRATION_3_4,
            MangaDBMigrations.MIGRATION_4_5,
            MangaDBMigrations.MIGRATION_5_6,
            MangaDBMigrations.MIGRATION_6_7,
        ).build()
    }

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            ReadMarkerDatabase::class.java, "readmarker"
        ).build()
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
