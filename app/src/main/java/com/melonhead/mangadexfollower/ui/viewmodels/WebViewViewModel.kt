package com.melonhead.mangadexfollower.ui.viewmodels

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.melonhead.mangadexfollower.extensions.asLiveData
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.ui.scenes.WebViewActivity

class WebViewViewModel(
    private val mangaRepository: MangaRepository,
): ViewModel() {
    private val mutableUrl: MutableLiveData<String?> = MutableLiveData(null)
    val url = mutableUrl.asLiveData()

    private lateinit var manga: UIManga
    private lateinit var chapter: UIChapter

    fun parseIntent(intent: Intent) {
        manga = intent.getParcelableExtra(WebViewActivity.EXTRA_UIMANGA)!!
        chapter = intent.getParcelableExtra(WebViewActivity.EXTRA_UICHAPTER)!!
        mutableUrl.value = chapter.webAddress
    }

    suspend fun markAsRead() {
        mangaRepository.markChapterRead(manga, chapter)
    }
}