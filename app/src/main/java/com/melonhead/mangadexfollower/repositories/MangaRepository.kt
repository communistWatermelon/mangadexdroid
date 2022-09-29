package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.db.chapter.ChapterDao
import com.melonhead.mangadexfollower.db.chapter.ChapterEntity
import com.melonhead.mangadexfollower.db.manga.MangaDao
import com.melonhead.mangadexfollower.db.manga.MangaEntity
import com.melonhead.mangadexfollower.extensions.throttleLatest
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
    private val refreshReadSeriesThrottled: (Pair<List<MangaEntity>, List<ChapterEntity>>) -> Unit = throttleLatest(300L, externalScope, ::refreshReadStatus)
    private val refreshMangaThrottled: (Unit) -> Unit = throttleLatest(300L, externalScope, ::refreshManga)

    // combine all manga series and chapters
    val manga = mangaDb.allSeries().combine(chapterDb.allChapters()) { dbSeries, dbChapters ->
        // map the series and chapters into UIManga, sorted from most recent to least
        dbSeries.map { manga ->
            val chapters = dbChapters.filter { it.mangaId == manga.id }.map { chapter ->
                UIChapter(id = chapter.id, chapter = chapter.chapter, title = chapter.chapterTitle, createdDate = chapter.createdAt, read = chapter.readStatus)
            }
            // refresh read status for series
            refreshReadSeriesThrottled(dbSeries to dbChapters)
            UIManga(id = manga.id, manga.mangaTitle ?: "", chapters = chapters)
        }.sortedByDescending { it.chapters.first().createdDate }
    }.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch {
            // refresh manga on login
            loginFlow.collectLatest { if (it) refreshMangaThrottled(Unit) }
        }
    }

    private fun refreshManga(unit: Unit) = externalScope.launch {
        val token = appDataService.token.firstOrNull() ?: return@launch

        val prevRefreshMs = appDataService.lastRefreshMs.firstOrNull() ?: 0L

        // fetch chapters from server
        val chapters = userService.getFollowedChapters(token, prevRefreshMs)
        val chapterEntities = chapters.data.map { ChapterEntity.from(it) }

        // add chapters to DB
        chapterDb.insertAll(*chapterEntities.toTypedArray())

        // list of series fetch jobs
        val jobs = mutableListOf<Deferred<Unit>>()

        val newManga = mutableSetOf<MangaEntity>()

        for (chapter in chapters.data) {
            // TODO: add some handling for too many chapters here, similar to read status below

            // find the manga for the series
            jobs.add(async {
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
    }

    private fun refreshReadStatus(contents: Pair<List<MangaEntity>, List<ChapterEntity>>) = externalScope.launch {
        // make sure we have a token
        val token = appDataService.token.firstOrNull() ?: return@launch

        val readChapters = mangaService.getReadChapters(contents.first.map { it.id }, token)
        val chaptersToUpdate = contents.second
            .filter { it.readStatus != true && readChapters.contains(it.id) }
            // make a copy with the readStatus set to true
            .map { it.copy(readStatus = true) }

        // update the db with the new entities
        for (chapterEntity in chaptersToUpdate) {
            chapterDb.update(chapterEntity)
        }
    }
}