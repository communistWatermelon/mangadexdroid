package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.models.UIManga
import com.melonhead.mangadexfollower.models.content.Manga
import com.melonhead.mangadexfollower.services.MangaService
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.services.UserService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MangaRepository(
    private val externalScope: CoroutineScope,
    private val mangaService: MangaService,
    private val userService: UserService,
    private val appDataService: AppDataService,
    private val loginFlow: Flow<Boolean>,
) {
    // TODO: move this into local db for more consistent caching
    private val cachedManga = hashMapOf<String, Manga>()

    private val mutableManga = MutableStateFlow<List<UIManga>>(listOf())
    val manga = mutableManga.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch {
            loginFlow.collectLatest { if (it) refreshManga() }
        }
    }

    private suspend fun refreshManga() {
        val token = appDataService.token.firstOrNull() ?: return
        val prevRefreshMs = appDataService.lastRefreshMs.firstOrNull() ?: 0L

        // TODO: pass publishAtSince to reduce load
        val mangaList = manga.replayCache.firstOrNull()?.toMutableList() ?: mutableListOf()

        val jobs = mutableListOf<Deferred<Unit>>()
        val chapters = userService.getFollowedChapters(token)
        for (chapter in chapters.data) {
            jobs.add(externalScope.async {
                val uiManga = chapter.relationships?.firstOrNull { it.type == "manga" } ?: return@async
                val mangaId = uiManga.id
                // fetch the manga title if it isn't cached
                var manga = cachedManga[mangaId]
                if (manga == null) {
                    manga = mangaService.getManga(mangaId)
                    // cache the manga
                    cachedManga[mangaId] = manga
                }

                // build/update the manga object
                var mangaObject = mangaList.firstOrNull { it.id == manga.id }
                if (mangaObject == null) {
                    mangaObject = UIManga(id = manga.id, manga.attributes.title.values.firstOrNull() ?: "", chapters = mutableListOf())
                    mangaList.add(mangaObject)
                }
                // add chapter to manga model
                mangaObject.chapters.add(chapter)
            })
        }

        jobs.awaitAll()
        mangaList.forEach { it.chapters.sortedBy { it.attributes.createdAt?.epochSeconds ?: 0 } }
        mutableManga.value = mangaList.sortedByDescending { it.chapters.first().attributes.createdAt?.epochSeconds }.toList()
    }
}