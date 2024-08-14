package com.melonhead.data_manga

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.melonhead.core.extensions.throttleLatest
import com.melonhead.data_app_data.AppDataService
import com.melonhead.data_at_home.AtHomeService
import com.melonhead.data_core_manga_ui.*
import com.melonhead.data_manga.services.MangaService
import com.melonhead.data_user.services.UserService
import com.melonhead.feature_chapter_cache.ChapterCacheMechanism
import com.melonhead.lib_app_context.AppContext
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.AppLifecycleEvent
import com.melonhead.lib_app_events.events.AuthenticationEvent
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_database.chapter.ChapterDao
import com.melonhead.lib_database.chapter.ChapterEntity
import com.melonhead.lib_database.extensions.from
import com.melonhead.lib_database.manga.MangaDao
import com.melonhead.lib_database.manga.MangaEntity
import com.melonhead.lib_database.readmarkers.ReadMarkerDao
import com.melonhead.lib_database.readmarkers.ReadMarkerEntity
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_notifications.NewChapterNotificationChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface MangaRepository {
    val manga: Flow<List<UIManga>>
    val refreshStatus: Flow<MangaRefreshStatus>
    suspend fun toggleChapterRead(uiManga: UIManga, uiChapter: UIChapter)
    suspend fun markChapterRead(uiManga: UIManga, uiChapter: UIChapter)
    suspend fun getChapterData(chapterId: String): List<String>?
    suspend fun setUseWebview(manga: UIManga, useWebView: Boolean)
    suspend fun updateChosenTitle(manga: UIManga, chosenTitle: String)
}

class MangaRepositoryImpl(
    private val externalScope: CoroutineScope,
    private val userService: UserService,
    private val appDataService: AppDataService,
    private val atHomeService: AtHomeService,
    private val chapterDb: ChapterDao,
    private val mangaDb: MangaDao,
    private val readMarkerDb: ReadMarkerDao,
    private val appContext: Context,
    private val chapterCache: ChapterCacheMechanism,
    private val appEventsRepository: AppEventsRepository,
    private val newChapterNotificationChannel: NewChapterNotificationChannel,
): MangaRepository, KoinComponent {
    private val mangaService: MangaService by inject()

    private val refreshMangaThrottled: (Unit) -> Unit = throttleLatest(300L, externalScope, ::refreshManga)

    // combine all manga series and chapters
    override val manga = combine(mangaDb.allSeries(), chapterDb.allChapters(), readMarkerDb.allMarkers()) { dbSeries, dbChapters, _ ->
        generateUIManga(dbSeries, dbChapters)
    }.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    private val mutableRefreshStatus = MutableStateFlow<MangaRefreshStatus>(None)
    override val refreshStatus = mutableRefreshStatus.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    private var isLoggedIn: Boolean = false

    init {
        externalScope.launch {
            // refresh manga on login
            try {
                appEventsRepository.events.collectLatest {
                    when (it) {
                        is AuthenticationEvent.LoggedIn -> {
                            isLoggedIn = true
                            refreshMangaThrottled(Unit)
                        }
                        is AuthenticationEvent.LoggedOut -> {
                            isLoggedIn = false
                        }
                        is AppLifecycleEvent.AppForegrounded -> {
                            forceRefresh()
                        }
                        is UserEvent.RefreshManga -> {
                            forceRefresh()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun forceRefresh() {
        if (isLoggedIn) refreshMangaThrottled(Unit)
    }

    private fun generateUIManga(dbSeries: List<MangaEntity>, dbChapters: List<ChapterEntity>): List<UIManga> {
        // map the series and chapters into UIManga, sorted from most recent to least
        val uiManga = dbSeries.mapNotNull { manga ->
            var hasExternalChapters = false
            val chapters = dbChapters.filter { it.mangaId == manga.id }.map { chapter ->
                val read = readMarkerDb.getEntity(chapter.mangaId, chapter.chapter)?.readStatus
                hasExternalChapters = hasExternalChapters || chapter.externalUrl != null
                UIChapter(
                    id = chapter.id,
                    chapter = chapter.chapter,
                    title = chapter.chapterTitle,
                    createdDate = chapter.createdAt.epochSeconds,
                    read = read,
                    externalUrl = chapter.externalUrl
                )
            }
            if (chapters.isEmpty()) return@mapNotNull null
            UIManga(
                id = manga.id,
                manga.chosenTitle ?: "",
                chapters = chapters,
                manga.mangaCoverId,
                useWebview = hasExternalChapters || manga.useWebview,
                altTitles = manga.mangaTitles,
                tags = manga.tags.sortedBy { it.id }.map { it.name },
                status = manga.status,
                contentRating = manga.contentRating,
                lastChapter = manga.lastChapter,
                description = manga.description,
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
        appEventsRepository.postEvent(AuthenticationEvent.RefreshToken())
        val token = appDataService.token.firstOrNull() ?: return@launch
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

            // fetch manga series info
            Clog.i("New manga: ${mangaIds.count()}")

            if (mangaIds.isNotEmpty()) {
                val mangaSeries = mangaService.getManga(token, mangaIds.toList())
                val manga = mangaSeries.map {
                    // grab the chosen title from the DB
                    MangaEntity.from(it, mangaDb.getMangaById(it.id).first()?.chosenTitle)
                }

                // insert new series into local db
                mangaDb.insertAll(*manga.toTypedArray())
            }
        }

        mutableRefreshStatus.value = ReadStatus
        // refresh read status for series
        refreshReadStatus()

        mutableRefreshStatus.value = None
        appDataService.updateLastRefreshDate()
    }

    private suspend fun notifyOfNewChapters() {
        if (AppContext.isInForeground) return
        val notificationManager = NotificationManagerCompat.from(appContext)
        if (!notificationManager.areNotificationsEnabled()) return
        val installDateSeconds = appDataService.installDateSeconds.firstOrNull() ?: 0L
        Clog.i("notifyOfNewChapters")

        val newChapters = chapterDb.getAllSync().filter { readMarkerDb.isRead(it.mangaId, it.chapter) != true }
        val manga = mangaDb.getAllSync()
        val notifyChapters = generateUIManga(manga, newChapters)
        chapterCache.cacheImagesForChapters(newChapters)
        newChapterNotificationChannel.post(appContext, notifyChapters, installDateSeconds)
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

    override suspend fun toggleChapterRead(uiManga: UIManga, uiChapter: UIChapter) {
        val token = appDataService.token.firstOrNull() ?: return
        val entity = readMarkerDb.getEntity(uiManga.id, uiChapter.chapter) ?: return

        val toggledStatus = !(entity.readStatus ?: false)
        if (toggledStatus) {
            newChapterNotificationChannel.dismissNotification(appContext, uiManga, uiChapter)
        }
        readMarkerDb.update(entity.copy(readStatus = toggledStatus))
        mangaService.changeReadStatus(token, uiManga, uiChapter, toggledStatus)
    }

    override suspend fun markChapterRead(uiManga: UIManga, uiChapter: UIChapter) {
        val token = appDataService.token.firstOrNull() ?: return
        val entity = readMarkerDb.getEntity(uiManga.id, uiChapter.chapter) ?: return
        newChapterNotificationChannel.dismissNotification(appContext, uiManga, uiChapter)
        readMarkerDb.update(entity.copy(readStatus = true))
        mangaService.changeReadStatus(token, uiManga, uiChapter, true)
    }

    override suspend fun getChapterData(chapterId: String): List<String>? {
        val token = appDataService.token.firstOrNull() ?: return null
        val chapterData = atHomeService.getChapterData(token, chapterId)
        return if (appDataService.useDataSaver) {
            chapterData?.pagesDataSaver()
        } else {
            chapterData?.pages()
        }
    }

    override suspend fun setUseWebview(manga: UIManga, useWebView: Boolean) {
        val entity = mangaDb.mangaById(manga.id).first() ?: return
        mangaDb.update(entity.copy(useWebview = useWebView))
    }

    override suspend fun updateChosenTitle(manga: UIManga, chosenTitle: String) {
        val entity = mangaDb.mangaById(manga.id).first() ?: return
        if (!entity.mangaTitles.contains(chosenTitle)) return
        mangaDb.update(entity.copy(chosenTitle = chosenTitle))
    }
}
