package com.melonhead.mangadexfollower.extensions

import com.melonhead.lib_database.chapter.ChapterEntity
import com.melonhead.lib_database.manga.MangaEntity
import com.melonhead.mangadexfollower.models.content.Chapter
import com.melonhead.mangadexfollower.models.content.Manga

fun ChapterEntity.Companion.from(chapter: Chapter): ChapterEntity {
    return ChapterEntity(
        id = chapter.id,
        mangaId = chapter.relationships?.firstOrNull { it.type == "manga" }!!.id,
        chapterTitle = chapter.attributes.title,
        chapter = chapter.attributes.chapter ?: "1",
        createdAt = chapter.attributes.createdAt,
        externalUrl = chapter.attributes.externalUrl,
    )
}

fun MangaEntity.Companion.from(manga: Manga): MangaEntity {
    val titles = manga.attributes.getEnglishTitles()
    return MangaEntity(
        id = manga.id,
        mangaTitles = titles,
        chosenTitle = titles.last(),
        mangaCoverId = manga.fileName,
        status = manga.attributes.status,
        tags = manga.attributes.tags.mapNotNull { it.attributes.name["en"] },
    )
}
