package com.melonhead.feature_native_chapter_viewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.melonhead.lib_core.models.UIChapter
import com.melonhead.lib_core.models.UIManga
import com.melonhead.lib_core.theme.MangadexFollowerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class ChapterActivity: ComponentActivity() {
    private val viewModel by viewModel<ChapterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MangadexFollowerTheme {
                val page by viewModel.currentPage.collectAsState(initial = null)
                val pages by viewModel.chapterData.collectAsState()
                ChapterScreen(
                    currentPage = page,
                    allPages = pages,
                    chapterTapAreaSize = viewModel.chapterTapAreaSize,
                    onCompletedChapter = {
                        viewModel.markAsRead()
                        finish()
                    },
                    nextPage = { viewModel.nextPage() },
                    prevPage = { viewModel.prevPage() }
                )
            }
        }
        viewModel.parseIntent(this, intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.parseIntent(this, intent)
    }

    companion object {
        const val EXTRA_UICHAPTER = "EXTRA_UICHAPTER"
        const val EXTRA_UICHAPTER_DATA = "EXTRA_UICHAPTER_DATA"
        const val EXTRA_UIMANGA = "EXTRA_UIMANGA"

        internal fun newIntent(context: Context, chapter: UIChapter, manga: UIManga, chapterData: List<String>): Intent {
            val intent = Intent(context, ChapterActivity::class.java)
            intent.putExtra(EXTRA_UIMANGA, manga)
            intent.putExtra(EXTRA_UICHAPTER, chapter)
            intent.putExtra(EXTRA_UICHAPTER_DATA, chapterData.toTypedArray())
            return intent
        }
    }
}
