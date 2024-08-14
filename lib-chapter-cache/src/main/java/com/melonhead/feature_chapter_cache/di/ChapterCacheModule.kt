package com.melonhead.feature_chapter_cache.di

import com.melonhead.feature_chapter_cache.ChapterCacheMechanism
import com.melonhead.feature_chapter_cache.ChapterCacheMechanismImpl
import com.melonhead.lib_database.di.DBModule
import org.koin.dsl.module

val ChapterCacheModule = module {
    includes(DBModule)
    single<ChapterCacheMechanism> {
        ChapterCacheMechanismImpl()
    }
}
