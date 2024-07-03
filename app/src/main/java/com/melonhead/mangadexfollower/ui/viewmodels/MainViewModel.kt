package com.melonhead.mangadexfollower.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.melonhead.mangadexfollower.extensions.asLiveData
import com.melonhead.mangadexfollower.extensions.dateOrTimeString
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.services.RenderStyle
import com.melonhead.mangadexfollower.ui.scenes.ChapterActivity
import com.melonhead.mangadexfollower.ui.scenes.WebViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class MainViewModel(
    private val authRepository: AuthRepository,
    private val mangaRepository: MangaRepository,
    private val userAppDataService: AppDataService
): ViewModel() {
    val loginStatus = authRepository.loginStatus.asLiveData()
    val manga = mangaRepository.manga.asLiveData()
    val refreshStatus = mangaRepository.refreshStatus.asLiveData()
    val readMangaCount = userAppDataService.showReadChapterCount

    private val mutableRefreshText = MutableLiveData<String>()
    val refreshText = mutableRefreshText.asLiveData()

    init {
        viewModelScope.launch {
            while (isActive) {
                updateRefreshText()
                delay(60000) // refresh update text every minute
            }
        }

        viewModelScope.launch {
            try {
                userAppDataService.lastRefreshDateSeconds.collectLatest {
                    updateRefreshText()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun updateRefreshText() {
        val lastRefreshDateSecond = userAppDataService.lastRefreshDateSeconds.first()
        mutableRefreshText.value = if (lastRefreshDateSecond != null)
            Instant.fromEpochSeconds(lastRefreshDateSecond).dateOrTimeString(useRelative = true)
        else
            "Never"
    }

    fun authenticate(email: String, password: String) = viewModelScope.launch {
        authRepository.authenticate(email, password)
    }

    fun onChapterClicked(context: Context, uiManga: UIManga, uiChapter: UIChapter) = viewModelScope.launch(Dispatchers.IO) {
        // mark chapter as read on tap only for browse style rendering
        if (userAppDataService.renderStyle == RenderStyle.Browser) {
            mangaRepository.markChapterRead(uiManga, uiChapter)
        }

        val intent = when (userAppDataService.renderStyle) {
            RenderStyle.Native -> ChapterActivity.newIntent(context, uiChapter, uiManga)
            RenderStyle.WebView -> WebViewActivity.newIntent(context, uiChapter, uiManga)
            RenderStyle.Browser -> Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uiChapter.webAddress) }
        }

        context.startActivity(intent)
    }

    fun toggleChapterRead(uiManga: UIManga, uiChapter: UIChapter) = viewModelScope.launch(Dispatchers.IO) {
        mangaRepository.toggleChapterRead(uiManga, uiChapter)
    }

    fun refreshContent() = viewModelScope.launch {
        mangaRepository.forceRefresh()
        delay(5000) // prevent another refresh for 5 second
    }

    fun toggleMangaWebview(uiManga: UIManga) = viewModelScope.launch {
        mangaRepository.setUseWebview(uiManga, !uiManga.useWebview)
    }
}