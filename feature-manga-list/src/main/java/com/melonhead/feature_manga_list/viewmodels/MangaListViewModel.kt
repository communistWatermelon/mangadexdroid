package com.melonhead.feature_manga_list.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.melonhead.lib_core.extensions.asLiveData
import com.melonhead.lib_core.extensions.dateOrTimeString
import com.melonhead.lib_core.models.UIChapter
import com.melonhead.lib_core.models.UIManga
import com.melonhead.data_app_data.AppDataService
import com.melonhead.data_app_data.RenderStyle
import com.melonhead.feature_manga_list.MangaRepository
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_navigation.Navigator
import com.melonhead.lib_navigation.keys.ActivityKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

internal class MangaListViewModel(
    private val mangaRepository: MangaRepository,
    private val userAppDataService: AppDataService,
    private val navigator: Navigator,
    private val appEventsRepository: AppEventsRepository,
): ViewModel() {
    val manga = mangaRepository.manga.asLiveData(viewModelScope.coroutineContext)
    val refreshStatus = mangaRepository.refreshStatus.asLiveData(viewModelScope.coroutineContext)
    val readMangaCount = userAppDataService.showReadChapterCount

    private val mutableRefreshText = MutableLiveData<String>()
    val refreshText = mutableRefreshText.asLiveData()

    init {
        viewModelScope.launch {
            appEventsRepository.events.collectLatest {
                if (it is UserEvent.OpenedNotification) {
                    onChapterClicked(it.context, it.manga, it.chapter)
                }
            }
        }

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

    private fun navigateToWebView(context: Context, uiManga: UIManga, uiChapter: UIChapter): Intent {
        return navigator.intentForKey(context, ActivityKey.WebViewActivity(
            Bundle().apply {
                putParcelable(ActivityKey.WebViewActivity.PARAM_MANGA, uiManga)
                putParcelable(ActivityKey.WebViewActivity.PARAM_CHAPTER, uiChapter)
            }
        ))
    }

    private suspend fun updateRefreshText() {
        val lastRefreshDateSecond = userAppDataService.lastRefreshDateSeconds.first()
        mutableRefreshText.value = if (lastRefreshDateSecond != null)
            Instant.fromEpochSeconds(lastRefreshDateSecond).dateOrTimeString(useRelative = true)
        else
            "Never"
    }

    fun onChapterClicked(context: Context, uiManga: UIManga, uiChapter: UIChapter) {
        viewModelScope.launch {
            val intent = when (userAppDataService.renderStyle) {
                RenderStyle.Native -> {
                    val chapterData = mangaRepository.getChapterData(uiManga.id, uiChapter.id)
                    // use secondary render style
                    if (chapterData.isNullOrEmpty()) {
                        appEventsRepository.postEvent(UserEvent.SetUseWebView(uiManga, true))
                        navigateToWebView(context, uiManga, uiChapter)
                    } else {
                        navigator.intentForKey(context, ActivityKey.ChapterActivity(
                            Bundle().apply {
                                putParcelable(ActivityKey.ChapterActivity.PARAM_MANGA, uiManga)
                                putParcelable(ActivityKey.ChapterActivity.PARAM_CHAPTER, uiChapter)
                                putStringArray(ActivityKey.ChapterActivity.PARAM_CHAPTER_DATA, chapterData.toTypedArray())
                            }
                        ))
                    }
                }
                RenderStyle.WebView -> navigateToWebView(context, uiManga, uiChapter)
                RenderStyle.Browser -> {
                    // mark chapter as read on tap only for browse style rendering
                    appEventsRepository.postEvent(UserEvent.SetMarkChapterRead(uiChapter, uiManga, !uiChapter.read!!))
                    Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(uiChapter.webAddress) }
                }
            }

            context.startActivity(intent)
        }
    }

    fun toggleChapterRead(uiManga: UIManga, uiChapter: UIChapter) = viewModelScope.launch(Dispatchers.IO) {
        appEventsRepository.postEvent(UserEvent.SetMarkChapterRead(uiChapter, uiManga, !uiChapter.read!!))
    }

    fun refreshContent() = viewModelScope.launch {
        appEventsRepository.postEvent(UserEvent.RefreshManga)
        delay(5000) // prevent another refresh for 5 second
    }

    fun toggleMangaWebview(uiManga: UIManga) = viewModelScope.launch {
        appEventsRepository.postEvent(UserEvent.SetUseWebView(uiManga, !uiManga.useWebview))
    }

    fun setMangaTitle(uiManga: UIManga, newTitle: String) = viewModelScope.launch {
        appEventsRepository.postEvent(UserEvent.UpdateChosenMangaTitle(uiManga, newTitle))
    }
}
