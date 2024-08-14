package com.melonhead.mangadexfollower.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.melonhead.data_app_data.AppDataService
import com.melonhead.data_app_data.RenderStyle
import com.melonhead.data_core_manga_ui.UIChapter
import com.melonhead.data_core_manga_ui.UIManga
import com.melonhead.data_manga.MangaRepository
import com.melonhead.feature_authentication.AuthRepository
import com.melonhead.feature_authentication.models.LoginStatus
import com.melonhead.core.extensions.asLiveData
import com.melonhead.core.extensions.dateOrTimeString
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.AuthenticationEvent
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_navigation.Navigator
import com.melonhead.lib_navigation.keys.ActivityKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class MainViewModel(
    private val authRepository: AuthRepository,
    // TODO: remove mangarepo dep
    private val mangaRepository: MangaRepository,
    // TODO: move relent info into mangarepo
    private val userAppDataService: AppDataService,
    private val appEventsRepository: AppEventsRepository,
    private val navigator: Navigator,
): ViewModel() {
    val loginStatus = appEventsRepository.events.mapNotNull {
        when (it) {
            is AuthenticationEvent.LoggedIn -> LoginStatus.LoggedIn
            is AuthenticationEvent.LoggedOut -> LoginStatus.LoggedOut
            is AuthenticationEvent.LoggingIn -> LoginStatus.LoggingIn
            else -> null
        }
    }.asLiveData(viewModelScope.coroutineContext)

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
        // TODO: replace with event?
        authRepository.authenticate(email, password)
    }

    fun onChapterClicked(context: Context, uiManga: UIManga, uiChapter: UIChapter) = viewModelScope.launch(Dispatchers.IO) {
        // mark chapter as read on tap only for browse style rendering
        if (userAppDataService.renderStyle == RenderStyle.Browser) {
            mangaRepository.markChapterRead(uiManga, uiChapter)
        }

        val intent = when (userAppDataService.renderStyle) {
            RenderStyle.Native -> navigator.intentForKey(context, ActivityKey.ChapterActivity(
                Bundle().apply {
                    putParcelable(ActivityKey.ChapterActivity.PARAM_MANGA, uiManga)
                    putParcelable(ActivityKey.ChapterActivity.PARAM_CHAPTER, uiChapter)
                }
            ))
            RenderStyle.WebView -> navigator.intentForKey(context, ActivityKey.WebViewActivity(
                Bundle().apply {
                    putParcelable(ActivityKey.WebViewActivity.PARAM_MANGA, uiManga)
                    putParcelable(ActivityKey.WebViewActivity.PARAM_CHAPTER, uiChapter)
                }
            ))
            RenderStyle.Browser -> Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uiChapter.webAddress) }
        }

        context.startActivity(intent)
    }

    fun toggleChapterRead(uiManga: UIManga, uiChapter: UIChapter) = viewModelScope.launch(Dispatchers.IO) {
        mangaRepository.toggleChapterRead(uiManga, uiChapter)
    }

    fun refreshContent() = viewModelScope.launch {
        appEventsRepository.postEvent(UserEvent.RefreshManga)
        delay(5000) // prevent another refresh for 5 second
    }

    fun toggleMangaWebview(uiManga: UIManga) = viewModelScope.launch {
        mangaRepository.setUseWebview(uiManga, !uiManga.useWebview)
    }

    fun setMangaTitle(uiManga: UIManga, newTitle: String) = viewModelScope.launch {
        mangaRepository.updateChosenTitle(uiManga, newTitle)
    }
}
