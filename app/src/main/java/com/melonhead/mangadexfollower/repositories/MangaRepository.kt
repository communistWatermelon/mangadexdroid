package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.db.chapter.ChapterDao
import com.melonhead.mangadexfollower.db.chapter.ChapterEntity
import com.melonhead.mangadexfollower.db.manga.MangaDao
import com.melonhead.mangadexfollower.db.manga.MangaEntity
import com.melonhead.mangadexfollower.extensions.throttleLatest
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.services.MangaService
import com.melonhead.mangadexfollower.services.UserService
import com.melonhead.mangadexfollower.models.ui.LoginStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MangaRepository(
    private val externalScope: CoroutineScope,
    private val mangaService: MangaService,
    private val userService: UserService,
    private val appDataService: AppDataService,
    private val loginFlow: Flow<LoginStatus>,
    private val chapterDb: ChapterDao,
    private val mangaDb: MangaDao
) {
    private val refreshReadSeriesThrottled: (Pair<List<MangaEntity>, List<ChapterEntity>>) -> Unit = throttleLatest(300L, externalScope, ::refreshReadStatus)
    private val refreshMangaThrottled: (Unit) -> Unit = throttleLatest(300L, externalScope, ::refreshManga)

    // combine all manga series and chapters
    val manga = mangaDb.allSeries().combine(chapterDb.allChapters()) { dbSeries, dbChapters ->
        // map the series and chapters into UIManga, sorted from most recent to least
        val uiManga = dbSeries.mapNotNull { manga ->
            val chapters = dbChapters.filter { it.mangaId == manga.id }.map { chapter ->
                UIChapter(id = chapter.id, chapter = chapter.chapter, title = chapter.chapterTitle, createdDate = chapter.createdAt, read = chapter.readStatus)
            }
            if (chapters.isEmpty()) return@mapNotNull null
            // refresh read status for series
            refreshReadSeriesThrottled(dbSeries to dbChapters)
            UIManga(id = manga.id, manga.mangaTitle ?: "", chapters = chapters)
        }
        if (uiManga.isNotEmpty()) uiManga.sortedByDescending { it.chapters.first().createdDate } else uiManga
    }.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch {
            // refresh manga on login
            loginFlow.collectLatest { if (it is LoginStatus.LoggedIn) refreshMangaThrottled(Unit) }
        }
    }

    // note: unit needs to be included as a param for the throttleLatest call above
    private fun refreshManga(@Suppress("UNUSED_PARAMETER") unit: Unit) = externalScope.launch {
        val token = appDataService.token.firstOrNull() ?: return@launch

        val prevRefreshMs = appDataService.lastRefreshMs.firstOrNull() ?: 0L

        // fetch chapters from server
        val chapters = userService.getFollowedChapters(token, prevRefreshMs)
        val chapterEntities = chapters.data.map { ChapterEntity.from(it) }

        // add chapters to DB
        chapterDb.insertAll(*chapterEntities.toTypedArray())

        // map app chapters into the manga ids
        val mangaIds = chapters.data.mapNotNull { it.relationships?.firstOrNull { it.type == "manga" }?.id }.toSet()
        // fetch manga series
        val newManga = mangaService.getManga(mangaIds.toList()).map { MangaEntity.from(it) }

        // insert new series into local db
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