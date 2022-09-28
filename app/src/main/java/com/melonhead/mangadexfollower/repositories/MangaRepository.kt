package com.melonhead.mangadexfollower.repositories

import android.util.Log
import com.melonhead.mangadexfollower.models.Manga
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
    private val mutableManga = MutableStateFlow<List<Manga>>(listOf())
    val manga = mutableManga.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch { refreshManga() }
        externalScope.launch {
            loginFlow.collectLatest { if (it) refreshManga() }
        }
    }

    private val idList = mutableSetOf<String>()
    private suspend fun refreshManga() {
        if (tokenProviderService.token == null) return
        val chapters = userService.getFollowedChapters()
        for (chapter in chapters.data) {
            idList.add(chapter.id)
        }

        Log.d("TAG", "refreshManga: ${idList.count()}")
    }
}