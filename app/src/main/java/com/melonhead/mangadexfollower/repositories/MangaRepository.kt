package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.db.chapter.ChapterDao
import com.melonhead.mangadexfollower.db.chapter.ChapterEntity
import com.melonhead.mangadexfollower.db.manga.MangaDao
import com.melonhead.mangadexfollower.db.manga.MangaEntity
import com.melonhead.mangadexfollower.models.UIChapter
import com.melonhead.mangadexfollower.models.UIManga
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.services.MangaService
import com.melonhead.mangadexfollower.services.UserService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MangaRepository(
    private val externalScope: CoroutineScope,
    private val mangaService: MangaService,
    private val userService: UserService,
    private val appDataService: AppDataService,
    private val loginFlow: Flow<Boolean>,
    private val chapterDb: ChapterDao,
    private val mangaDb: MangaDao
) {
    val manga = mangaDb.allSeries().combine(chapterDb.allChapters()) { dbSeries, dbChapters ->
        dbSeries.map { manga ->
            val chapters = dbChapters.filter { it.mangaId == manga.id }.map { chapter ->
                UIChapter(id = chapter.id, chapter = chapter.chapter, title = chapter.chapterTitle, createdDate = chapter.createdAt, read = chapter.readStatus)
            }
            UIManga(id = manga.id, manga.mangaTitle ?: "", chapters = chapters)
        }.sortedByDescending { it.chapters.first().createdDate }
    }.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch {
            loginFlow.collectLatest { if (it) refreshManga() }
        }
    }

    private suspend fun refreshManga() {
        val token = appDataService.token.firstOrNull() ?: return
        val prevRefreshMs = appDataService.lastRefreshMs.firstOrNull() ?: 0L

        // fetch chapters from server
        val chapters = userService.getFollowedChapters(token, prevRefreshMs)

        // add chapters to DB
        chapterDb.insertAll(*chapters.data.map { ChapterEntity.from(it) }.toTypedArray())

        // list of series fetch jobs
        val jobs = mutableListOf<Deferred<Unit>>()

        val newManga = mutableSetOf<MangaEntity>()

        for (chapter in chapters.data) {
            // find the manga for the series
            jobs.add(externalScope.async {
                // find the manga relationship for the chapter
                val uiManga = chapter.relationships?.firstOrNull { it.type == "manga" } ?: return@async
                val mangaId = uiManga.id

                // ensure the db doesn't already contain this manga
                if (mangaDb.containsManga(mangaId)) return@async

                // fetch the manga from the server if it isn't cached
                val manga = mangaService.getManga(mangaId)

                // add all manga to list
                val entity = MangaEntity.from(manga)
                newManga.add(entity)
            })
        }

        jobs.awaitAll()
        mangaDb.insertAll(*newManga.toTypedArray())
        appDataService.updateLastRefreshMs(System.currentTimeMillis())

        // TODO: refresh read status for chapters
    }
}