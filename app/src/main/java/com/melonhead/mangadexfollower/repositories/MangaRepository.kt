package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.models.UIManga
import com.melonhead.mangadexfollower.models.content.Manga
import com.melonhead.mangadexfollower.services.MangaService
import com.melonhead.mangadexfollower.services.TokenProviderService
import com.melonhead.mangadexfollower.services.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MangaRepository(
    private val externalScope: CoroutineScope,
    private val mangaService: MangaService,
    private val userService: UserService,
    private val tokenProviderService: TokenProviderService,
    private val loginFlow: Flow<Boolean>
) {
    // TODO: move this into local db for more consistent caching
    private val cachedManga = hashMapOf<String, Manga>()

    private val mutableManga = MutableStateFlow<List<UIManga>>(listOf())
    val manga = mutableManga.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch { refreshManga() }
        externalScope.launch {
            loginFlow.collectLatest { if (it) refreshManga() }
        }
    }

    private suspend fun refreshManga() {
        if (tokenProviderService.token == null) return

        // TODO: pass publishAtSince to reduce load
        val mangaList = manga.replayCache.firstOrNull()?.toMutableList() ?: mutableListOf()

        val chapters = userService.getFollowedChapters()
        // TODO: make this async
        for (chapter in chapters.data) {
            val uiManga = chapter.relationships?.firstOrNull { it.type == "manga" } ?: continue
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
            // TODO: sort by date
        }

        mutableManga.value = mangaList.toList()
    }
}