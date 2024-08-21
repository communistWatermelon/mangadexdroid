package com.melonhead.lib_chapter_cache.di

import com.melonhead.lib_chapter_cache.ChapterCacheMechanism
import com.melonhead.lib_chapter_cache.ChapterCacheMechanismImpl
import com.melonhead.lib_database.di.LibDbModule
import org.koin.dsl.module

val LibChapterCacheModule = module {
    includes(LibDbModule)
    single<ChapterCacheMechanism> {
        ChapterCacheMechanismImpl()
    }
}
