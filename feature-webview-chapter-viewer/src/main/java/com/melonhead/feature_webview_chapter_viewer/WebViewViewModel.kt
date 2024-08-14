package com.melonhead.feature_webview_chapter_viewer

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.melonhead.core.extensions.asLiveData
import com.melonhead.data_core_manga_ui.UIChapter
import com.melonhead.data_core_manga_ui.UIManga
import com.melonhead.data_manga.MangaRepository

class WebViewViewModel(
    private val mangaRepository: MangaRepository,
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

    suspend fun markAsRead() {
        mangaRepository.markChapterRead(manga, chapter)
    }
}
