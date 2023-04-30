package com.melonhead.mangadexfollower.ui.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.ui.scenes.ChapterActivity
import com.melonhead.mangadexfollower.ui.scenes.WebViewActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Integer.min

class ChapterViewModel(
    private val mangaRepository: MangaRepository,
    appDataService: AppDataService
): ViewModel() {
    private val mutableChapterData = MutableStateFlow<List<String>?>(null)
    val chapterData = mutableChapterData.asStateFlow()

    val chapterTapAreaSize = appDataService.chapterTapAreaSize

    private val mutablePageIndex = MutableStateFlow(0)
    val currentPage = mutableChapterData.combine(mutablePageIndex) { data, index -> data?.getOrNull(index) }

    private lateinit var manga: UIManga
    private lateinit var chapter: UIChapter

    private fun loadChapter(chapterId: String) {
        viewModelScope.launch {
            val pages = mangaRepository.getChapterData(chapterId) // TODO: throw an error instead
            if (pages != null) {
                mutableChapterData.value = pages
            }
        }
    }

    fun prevPage() {
        mutablePageIndex.value = max(0, mutablePageIndex.value - 1)
    }

    fun nextPage() {
        mutablePageIndex.value = min((mutableChapterData.value?.count()?.minus(1)) ?: 0, mutablePageIndex.value + 1)
        // TODO: close if at end and no following chapter?
    }

    fun parseIntent(intent: Intent) {
        manga = intent.getParcelableExtra(ChapterActivity.EXTRA_UIMANGA)!!
        chapter = intent.getParcelableExtra(ChapterActivity.EXTRA_UICHAPTER)!!
        loadChapter(chapter.id)
    }

    suspend fun markAsRead() {
        mangaRepository.markChapterRead(manga, chapter)
    }
}