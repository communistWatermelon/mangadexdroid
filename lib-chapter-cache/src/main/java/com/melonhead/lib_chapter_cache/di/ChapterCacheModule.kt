package com.melonhead.lib_chapter_cache.di

import com.melonhead.lib_chapter_cache.ChapterCacheMechanism
import com.melonhead.lib_chapter_cache.ChapterCacheMechanismImpl
import org.koin.dsl.module

val ChapterCacheModule = module {
    single<ChapterCacheMechanism> {
        ChapterCacheMechanismImpl()
    }
}
