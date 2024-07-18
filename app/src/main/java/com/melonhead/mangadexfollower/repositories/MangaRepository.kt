package com.melonhead.mangadexfollower.repositories

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.melonhead.mangadexfollower.App
import com.melonhead.lib_database.chapter.ChapterDao
import com.melonhead.lib_database.chapter.ChapterEntity
import com.melonhead.lib_database.manga.MangaDao
import com.melonhead.lib_database.manga.MangaEntity
import com.melonhead.lib_database.readmarkers.ReadMarkerDao
import com.melonhead.lib_database.readmarkers.ReadMarkerEntity
import com.melonhead.mangadexfollower.extensions.from
import com.melonhead.mangadexfollower.extensions.throttleLatest
import com.melonhead.lib_logging.Clog
import com.melonhead.mangadexfollower.models.ui.*
import com.melonhead.mangadexfollower.notifications.NewChapterNotification
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.services.AtHomeService
import com.melonhead.mangadexfollower.services.MangaService
import com.melonhead.mangadexfollower.services.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MangaRepository(
    private val externalScope: CoroutineScope,
    private val mangaService: MangaService,
    private val userService: UserService,
    private val appDataService: AppDataService,
    private val atHomeService: AtHomeService,
    private val chapterDb: ChapterDao,
    private val mangaDb: MangaDao,
    private val readMarkerDb: ReadMarkerDao,
    private val appContext: Context
): KoinComponent {
    private val authRepository: AuthRepository by inject()
    private val refreshMangaThrottled: (Unit) -> Unit = throttleLatest(300L, externalScope, ::refreshManga)

    // combine all manga series and chapters
    val manga = combine(mangaDb.allSeries(), chapterDb.allChapters(), readMarkerDb.allMarkers()) { dbSeries, dbChapters, _ ->
        generateUIManga(dbSeries, dbChapters)
    }.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    private val mutableRefreshStatus = MutableStateFlow<MangaRefreshStatus>(None)
    val refreshStatus = mutableRefreshStatus.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch {
            // refresh manga on login
            try {
                authRepository.loginStatus.collectLatest { if (it is LoginStatus.LoggedIn) { refreshMangaThrottled(Unit) } }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun forceRefresh() {
        if (authRepository.loginStatus.first() is LoginStatus.LoggedIn) refreshMangaThrottled(Unit)
    }

    private fun generateUIManga(dbSeries: List<MangaEntity>, dbChapters: List<ChapterEntity>): List<UIManga> {
        // map the series and chapters into UIManga, sorted from most recent to least
        val uiManga = dbSeries.mapNotNull { manga ->
            var hasExternalChapters = false
            val chapters = dbChapters.filter { it.mangaId == manga.id }.map { chapter ->
                val read = readMarkerDb.getEntity(chapter.mangaId, chapter.chapter)?.readStatus
                hasExternalChapters = hasExternalChapters || chapter.externalUrl != null
                UIChapter(id = chapter.id, chapter = chapter.chapter, title = chapter.chapterTitle, createdDate = chapter.createdAt.epochSeconds, read = read, externalUrl = chapter.externalUrl)
            }
            if (chapters.isEmpty()) return@mapNotNull null
            UIManga(
                id = manga.id,
                manga.chosenTitle ?: "",
                chapters = chapters,
                manga.mangaCoverId,
                useWebview = hasExternalChapters || manga.useWebview,
                altTitles = manga.mangaTitles,
                tags = manga.tags,
                status = manga.status,
            )
        }
        if (uiManga.isEmpty()) return emptyList()

        // split into two categories, unread and read
        val hasUnread = uiManga.filter { it.chapters.any { it.read != true } }.sortedByDescending { it.chapters.first().createdDate }
        val allRead = uiManga.filter { it.chapters.all { it.read == true } }.sortedByDescending { it.chapters.first().createdDate }

        return hasUnread + allRead
    }

    // note: unit needs to be included as a param for the throttleLatest call above
    private fun refreshManga(@Suppress("UNUSED_PARAMETER") unit: Unit) = externalScope.launch {
        // refresh auth
        val token = authRepository.refreshToken()
        if (token == null) {
            Clog.i("Failed to refresh token")
            return@launch
        }

        Clog.i("refreshManga")

        mutableRefreshStatus.value = Following
        // fetch chapters from server
        val chaptersResponse = userService.getFollowedChapters(token)
        val chapterEntities = chaptersResponse.map { ChapterEntity.from(it) }
        val newChapters = chapterEntities.filter { !chapterDb.containsChapter(it.id) }

        Clog.i("New chapters: ${newChapters.count()}")

        if (newChapters.isNotEmpty()) {
            mutableRefreshStatus.value = MangaSeries
            // add chapters to DB
            chapterDb.insertAll(*newChapters.toTypedArray())

            // map app chapters into the manga ids
            val mangaIds = chaptersResponse.mapNotNull { chapters -> chapters.relationships?.firstOrNull { it.type == "manga" }?.id }.toSet()

            // fetch manga series
            val newMangaIds = mangaIds.filter { !mangaDb.containsManga(it) }
            Clog.i("New manga: ${newMangaIds.count()}")

            if (newMangaIds.isNotEmpty()) {
                val newMangaSeries = mangaService.getManga(token, newMangaIds.toList())
                val newManga = newMangaSeries.map { MangaEntity.from(it) }

                // insert new series into local db
                mangaDb.insertAll(*newManga.toTypedArray())
            }
        }

        mutableRefreshStatus.value = ReadStatus
        // refresh read status for series
        refreshReadStatus()

        mutableRefreshStatus.value = None
        appDataService.updateLastRefreshDate()
    }

    private suspend fun notifyOfNewChapters() {
        if ((appContext as App).inForeground) return
        val notificationManager = NotificationManagerCompat.from(appContext)
        if (!notificationManager.areNotificationsEnabled()) return
        val installDateSeconds = appDataService.installDateSeconds.firstOrNull() ?: 0L
        Clog.i("notifyOfNewChapters")

        val newChapters = chapterDb.getAllSync().filter { readMarkerDb.isRead(it.mangaId, it.chapter) != true }
        val manga = mangaDb.getAllSync()
        val notifyChapters = generateUIManga(manga, newChapters)
        NewChapterNotification.post(appContext, notifyChapters, installDateSeconds)
    }

    private suspend fun refreshReadStatus() {
        // make sure we have a token
        val token = appDataService.token.firstOrNull() ?: return
        Clog.i("refreshReadStatus")
        val manga = mangaDb.getAllSync()
        val chapters = chapterDb.getAllSync()

        // ensure all chapters have read markers
        val readMarkers = chapters.map { ReadMarkerEntity.from(it, null) }
        readMarkerDb.insertAll(*readMarkers.toTypedArray())

        val readChapters = mangaService.getReadChapters(token, manga.map { it.id })
        val chaptersToUpdate = chapters
            // filter out chapters already marked as read in the db
            .filter {
                val readStatus = readMarkerDb.isRead(it.mangaId, it.chapter)
                readStatus == null && readChapters.contains(it.id)
            }

        if (chaptersToUpdate.isEmpty()) {
            notifyOfNewChapters()
            return
        }

        // update the db with the new entities
        chapterDb.update(*chaptersToUpdate.toTypedArray())

        val readMarkersToUpdate = chaptersToUpdate
            .filter {
                val readStatus = readMarkerDb.isRead(it.mangaId, it.chapter)
                readStatus == null
            }
            .map { ReadMarkerEntity.from(it, true) }
        readMarkerDb.update(*readMarkersToUpdate.toTypedArray())

        // notify user of new chapters
        notifyOfNewChapters()
    }

    suspend fun toggleChapterRead(uiManga: UIManga, uiChapter: UIChapter) {
        val token = appDataService.token.firstOrNull() ?: return
        val entity = readMarkerDb.getEntity(uiManga.id, uiChapter.chapter) ?: return

        val toggledStatus = !(entity.readStatus ?: false)
        if (toggledStatus) {
            NewChapterNotification.dismissNotification(appContext, uiManga, uiChapter)
        }
        readMarkerDb.update(entity.copy(readStatus = toggledStatus))
        mangaService.changeReadStatus(token, uiManga, uiChapter, toggledStatus)
    }

    suspend fun markChapterRead(uiManga: UIManga, uiChapter: UIChapter) {
        val token = appDataService.token.firstOrNull() ?: return
        val entity = readMarkerDb.getEntity(uiManga.id, uiChapter.chapter) ?: return
        NewChapterNotification.dismissNotification(appContext, uiManga, uiChapter)
        readMarkerDb.update(entity.copy(readStatus = true))
        mangaService.changeReadStatus(token, uiManga, uiChapter, true)
    }

    suspend fun getChapterData(chapterId: String): List<String>? {
        val token = appDataService.token.firstOrNull() ?: return null
        val chapterData = atHomeService.getChapterData(token, chapterId)
        return if (appDataService.useDataSaver) {
            chapterData?.pagesDataSaver()
        } else {
            chapterData?.pages()
        }
    }

    suspend fun setUseWebview(manga: UIManga, useWebView: Boolean) {
        val entity = mangaDb.mangaById(manga.id).first() ?: return
        mangaDb.update(entity.copy(useWebview = useWebView))
    }

    suspend fun updateChosenTitle(manga: UIManga, chosenTitle: String) {
        val entity = mangaDb.mangaById(manga.id).first() ?: return
        if (!entity.mangaTitles.contains(chosenTitle)) return
        mangaDb.update(entity.copy(chosenTitle = chosenTitle))
    }
}
