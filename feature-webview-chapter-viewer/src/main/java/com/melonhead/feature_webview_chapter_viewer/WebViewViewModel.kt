package com.melonhead.feature_webview_chapter_viewer

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.melonhead.core.extensions.asLiveData
import com.melonhead.core_ui.models.UIChapter
import com.melonhead.core_ui.models.UIManga
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.UserEvent

class WebViewViewModel(
    private val appEventsRepository: AppEventsRepository,
): ViewModel() {
    private val mutableUrl: MutableLiveData<String?> = MutableLiveData(null)
    val url = mutableUrl.asLiveData()

    private lateinit var manga: UIManga
    private lateinit var chapter: UIChapter

    @Suppress("DEPRECATION")
    fun parseIntent(intent: Intent) {
        manga = intent.getParcelableExtra(WebViewActivity.EXTRA_UIMANGA)!!
        chapter = intent.getParcelableExtra(WebViewActivity.EXTRA_UICHAPTER)!!
        mutableUrl.value = chapter.externalUrl ?: chapter.webAddress
    }

    fun markAsRead() {
        appEventsRepository.postEvent(UserEvent.SetMarkChapterRead(chapter, manga, true))
    }
}
