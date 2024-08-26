package com.melonhead.feature_manga_list

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.melonhead.data_at_home.AtHomeService
import com.melonhead.data_manga.services.MangaService
import com.melonhead.data_shared.models.ui.*
import com.melonhead.data_user.services.UserService
import com.melonhead.lib_app_context.AppContext
import com.melonhead.lib_app_data.AppData
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.AppLifecycleEvent
import com.melonhead.lib_app_events.events.AuthenticationEvent
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_chapter_cache.ChapterCache
import com.melonhead.lib_core.extensions.throttleLatest
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

internal interface MangaRepository {
    val manga: Flow<List<UIManga>>
    val refreshStatus: Flow<MangaRefreshStatus>
    suspend fun getChapterData(mangaId: String,chapterId: String): List<String>?
}

internal class MangaRepositoryImpl(
    private val externalScope: CoroutineScope,
    private val userService: UserService,
    private val appData: AppData,
    private val atHomeService: AtHomeService,
    private val chapterDb: ChapterDao,
    private val mangaDb: MangaDao,
    private val readMarkerDb: ReadMarkerDao,
    private val context: Context,
    private val chapterCache: ChapterCache,
    private val appEventsRepository: AppEventsRepository,
    private val newChapterNotificationChannel: NewChapterNotificationChannel,
    private val appContext: AppContext,
): MangaRepository, KoinComponent {
    private val mangaService: MangaService by inject()

    private val refreshMangaThrottled: (Unit) -> Unit = throttleLatest(300L, externalScope, ::refreshManga)

    // combine all manga series and chapters
    override val manga = combine(mangaDb.allSeries(), chapterDb.allChapters(), readMarkerDb.allMarkers(), chapterCache.cachingStatus) { dbSeries, dbChapters, _, cacheStatus ->
        generateUIManga(dbSeries, dbChapters)
    }.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    private val mutableRefreshStatus = MutableStateFlow<MangaRefreshStatus>(None)
    override val refreshStatus = mutableRefreshStatus.shareIn(externalScope, replay = 0, started = SharingStarted.WhileSubscribed())

    private var isLoggedIn: Boolean = false

    init {
        externalScope.launch {
            // refresh manga on login
            try {
                // TODO: it's easy to miss necessary events with this pattern, it would be better to include a way to pass in the list of expected events
                appEventsRepository.events.collectLatest {
                    launch {
                        when (it) {
                            is AuthenticationEvent.LoggedIn -> {
                                if (!isLoggedIn) {
                                    isLoggedIn = true
                                    forceRefresh()
                                }
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
                            is UserEvent.SetMarkChapterRead -> {
                                markChapterRead(it.mangaId, it.chapterId, it.read)
                            }
                            is UserEvent.SetUseWebView -> {
                                setUseWebview(it.mangaId, it.useWebView)
                            }
                            is UserEvent.UpdateChosenMangaTitle -> {
                                updateChosenTitle(it.mangaId, it.title)
                            }
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
                val read = readMarkerDb.getEntityByChapter(chapter.mangaId, chapter.chapter)?.readStatus == true
                hasExternalChapters = hasExternalChapters || chapter.externalUrl != null
                UIChapter(
                    id = chapter.id,
                    chapter = chapter.chapter,
                    title = chapter.chapterTitle,
                    createdDate = chapter.createdAt.epochSeconds,
                    read = read,
                    externalUrl = chapter.externalUrl,
                    cachedPages = chapterCache.getChapterPageCountFromCache(manga.id, chapter.id)
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
        val token = appData.getToken()
        if (token == null) {
            Clog.i("Failed to refresh token")
            return@launch
        }

        Clog.i("refreshManga")

        mutableRefreshStatus.value = Following
        // fetch chapters from server
        val chaptersResponse = userService.getFollowedChapters()
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
                val mangaSeries = mangaService.getManga(mangaIds.toList())
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
        appData.updateLastRefreshDate()
    }

    private suspend fun handleUnreadChapters() {
        mutableRefreshStatus.value = FetchingChapters
        val manga = mangaDb.getAllSync()
        val newChapters = chapterDb.getAllSync().filter { readMarkerDb.isRead(it.mangaId, it.chapter) != true }
        chapterCache.cacheImagesForChapters(manga, newChapters)

        if (appContext.isInForeground) return
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return
        val installDateSeconds = appData.installDateSeconds.firstOrNull() ?: 0L
        Clog.i("Posting notification for new chapters")

        val notifyChapters = generateUIManga(manga, newChapters)
        newChapterNotificationChannel.post(context, notifyChapters, installDateSeconds)
    }

    private suspend fun refreshReadStatus() {
        Clog.i("refreshReadStatus")
        val manga = mangaDb.getAllSync()
        val chapters = chapterDb.getAllSync()

        // ensure all chapters have read markers
        val readMarkers = chapters.map { ReadMarkerEntity.from(it, null) }
        readMarkerDb.insertAll(*readMarkers.toTypedArray())

        val readChapters = mangaService.getReadChapters(manga.map { it.id })
        val chaptersToUpdate = chapters
            // filter out chapters already marked as read in the db
            .filter {
                val readStatus = readMarkerDb.isRead(it.mangaId, it.chapter)
                readStatus == null && readChapters.contains(it.id)
            }

        if (chaptersToUpdate.isEmpty()) {
            handleUnreadChapters()
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
        handleUnreadChapters()
    }

    // currently trying to deprecate this function, and use chapterCache directly
    override suspend fun getChapterData(mangaId: String, chapterId: String): List<String>? {
        val chapterFiles = chapterCache.getChapterFromCache(mangaId, chapterId)
        if (chapterFiles.isNotEmpty()) return chapterFiles

        Clog.i("Chapter not found in cache: $mangaId, $chapterId")
        Clog.e("Chapter not found in cache", RuntimeException("Chapter not found in cache"))
        val chapterData = atHomeService.getChapterData(chapterId)
        return if (appData.useDataSaver) {
            chapterData?.pagesDataSaver()
        } else {
            chapterData?.pages()
        }
    }

    private fun markChapterRead(mangaId: String, chapterId: String, read: Boolean) {
        externalScope.launch {
            val chapter = chapterDb.getChapterForId(chapterId)
            val entity = readMarkerDb.getEntityByChapter(
                mangaId = mangaId,
                chapter = chapter.chapter
            ) ?: return@launch
            if (read) {
                newChapterNotificationChannel.dismissNotification(context, mangaId, chapterId)
                chapterCache.clearChapterFromCache(mangaId = mangaId, chapterId = chapterId)
            }
            readMarkerDb.update(entity.copy(readStatus = read))
            mangaService.changeReadStatus(
                mangaId = mangaId,
                chapterId = chapterId,
                readStatus = read
            )
        }
    }

    private fun setUseWebview(mangaId: String, useWebView: Boolean) {
        externalScope.launch {
            val entity = mangaDb.mangaById(mangaId).first() ?: return@launch
            mangaDb.update(entity.copy(useWebview = useWebView))
        }
    }

    private fun updateChosenTitle(mangaId: String, chosenTitle: String) {
        externalScope.launch {
            val entity = mangaDb.mangaById(mangaId).first() ?: return@launch
            if (!entity.mangaTitles.contains(chosenTitle)) return@launch
            mangaDb.update(entity.copy(chosenTitle = chosenTitle))
        }
    }
}
