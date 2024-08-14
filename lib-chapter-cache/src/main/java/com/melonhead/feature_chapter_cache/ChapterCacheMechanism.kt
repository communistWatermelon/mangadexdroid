package com.melonhead.feature_chapter_cache

import com.melonhead.lib_database.chapter.ChapterEntity

interface ChapterCacheMechanism {
    fun cacheImagesForChapters(chapters: List<ChapterEntity>)
    fun getChapterFromCache(chapterId: String): List<String>
}

internal class ChapterCacheMechanismImpl : ChapterCacheMechanism {
    override fun cacheImagesForChapters(chapters: List<ChapterEntity>) {
        //TODO("Not yet implemented")
    }

    override fun getChapterFromCache(chapterId: String): List<String> {
        //TODO("Not yet implemented")
        return emptyList()
    }

}
