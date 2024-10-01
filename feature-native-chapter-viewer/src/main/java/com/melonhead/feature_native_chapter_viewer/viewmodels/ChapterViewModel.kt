package com.melonhead.feature_native_chapter_viewer.viewmodels

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melonhead.data_shared.models.ui.UIChapter
import com.melonhead.data_shared.models.ui.UIManga
import com.melonhead.lib_app_data.AppData
import com.melonhead.feature_native_chapter_viewer.ChapterActivity
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_navigation.Navigator
import com.melonhead.lib_navigation.keys.ActivityKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.Integer.max
import java.lang.Integer.min

internal class ChapterViewModel(
    private val appEventsRepository: AppEventsRepository,
    private val navigator: Navigator,
    appData: AppData
): ViewModel() {
    private val mutableChapterData = MutableStateFlow<List<String>?>(null)
    val chapterData = mutableChapterData.asStateFlow()

    val chapterTapAreaSize = appData.chapterTapAreaSize

    private val mutablePageIndex = MutableStateFlow(0)
    val currentPage = mutableChapterData.combine(mutablePageIndex) { data, index -> data?.getOrNull(index) }

    private lateinit var manga: UIManga
    private lateinit var chapter: UIChapter
    private lateinit var chapterPagesData: List<String>

    val longStrip: Boolean
        get() = manga.longStrip

    private val chapterLoadMutex = Mutex()

    private fun loadChapter(activity: Activity) {
        viewModelScope.launch {
            chapterLoadMutex.withLock {
                if (mutableChapterData.value != null) return@withLock
                val pages = if (manga.useWebview) null else chapterPagesData
                if (pages != null) {
                    mutableChapterData.value = pages
                } else {
                    Clog.i("Falling back to webview")
                    // fallback to secondary render style
                    val intent = navigator.intentForKey(activity, ActivityKey.WebViewActivity(Bundle().apply {
                        putParcelable(ActivityKey.WebViewActivity.PARAM_CHAPTER, chapter)
                        putParcelable(ActivityKey.WebViewActivity.PARAM_MANGA, manga)
                    }))
                    activity.finish()
                    activity.startActivity(intent)
                }
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

    @Suppress("DEPRECATION")
    fun parseIntent(activity: Activity, intent: Intent) {
        manga = intent.getParcelableExtra(ChapterActivity.EXTRA_UIMANGA)!!
        chapter = intent.getParcelableExtra(ChapterActivity.EXTRA_UICHAPTER)!!
        chapterPagesData = intent.getStringArrayExtra(ChapterActivity.EXTRA_UICHAPTER_DATA)!!.toList()
        loadChapter(activity)
    }

    fun markAsRead() {
        appEventsRepository.postEvent(UserEvent.SetMarkChapterRead(chapter.id, manga.id, true))
    }
}
