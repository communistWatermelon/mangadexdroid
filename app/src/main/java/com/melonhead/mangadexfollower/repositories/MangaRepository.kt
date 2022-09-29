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
    private var refreshReadStatusJob: Job? = null
    private var refreshMangaJob: Job? = null

    // combine all manga series and chapters
    val manga = mangaDb.allSeries().combine(chapterDb.allChapters()) { dbSeries, dbChapters ->
        // map the series and chapters into UIManga, sorted from most recent to least
        dbSeries.map { manga ->
            val chapters = dbChapters.filter { it.mangaId == manga.id }.map { chapter ->
                UIChapter(id = chapter.id, chapter = chapter.chapter, title = chapter.chapterTitle, createdDate = chapter.createdAt, read = chapter.readStatus)
            }
            // refresh read status for series
            refreshReadStatus(dbSeries, dbChapters)
            UIManga(id = manga.id, manga.mangaTitle ?: "", chapters = chapters)
        }.sortedByDescending { it.chapters.first().createdDate }
    }.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch {
            // refresh manga on login
            loginFlow.collectLatest { if (it) refreshManga() }
        }
    }

    private fun refreshManga() {
        if (refreshMangaJob != null) refreshMangaJob?.cancel()
        refreshMangaJob = externalScope.launch {
            val token = appDataService.token.firstOrNull()
            if (token == null) {
                refreshMangaJob = null
                return@launch
            }

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
            refreshMangaJob = null
        }
    }

    // list of read status fetch jobs
    private val asyncReadJobs = mutableListOf<Deferred<Unit>>()
    private fun refreshReadStatus(mangaSeries: List<MangaEntity>, chapters: List<ChapterEntity>) {
        if (refreshReadStatusJob?.isActive == true) {
            refreshReadStatusJob?.cancel()
            asyncReadJobs.forEach { it.cancel() }
            asyncReadJobs.clear()
        }
        refreshReadStatusJob = externalScope.launch {
            // make sure we have a token
            val token = appDataService.token.firstOrNull()
            if (token == null) {
                refreshReadStatusJob = null
                return@launch
            }

            // loop through all manga
            for (manga in mangaSeries) {
                // avoid slamming the server
                if (asyncReadJobs.count() > 10) break
                asyncReadJobs.add(async {
                    // get read status for chapters in manga
                    val readChapters = mangaService.getReadChapters(manga.id, token)

                    // find all chapters that are unread and are contained in the readChapters list
                    val chaptersToUpdate = chapters
                        .filter { it.readStatus != true && readChapters.contains(it.id) }
                        // make a copy with the readStatus set to true
                        .map { it.copy(readStatus = true) }

                    // update the db with the new entities
                    for (chapterEntity in chaptersToUpdate) {
                        chapterDb.update(chapterEntity)
                    }
                })
            }

            // wait for all jobs to complete
            asyncReadJobs.awaitAll()
            asyncReadJobs.clear()
            refreshReadStatusJob = null
        }
    }
}