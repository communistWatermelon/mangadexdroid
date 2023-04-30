package com.melonhead.mangadexfollower.ui.scenes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.lifecycleScope
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.ui.scenes.shared.CloseBanner
import com.melonhead.mangadexfollower.ui.scenes.shared.LoadingScreen
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import com.melonhead.mangadexfollower.ui.viewmodels.ChapterViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChapterActivity: ComponentActivity() {
    private val viewModel by viewModel<ChapterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MangadexFollowerTheme {
                val page by viewModel.currentPage.collectAsState(initial = null)
                val pages by viewModel.chapterData.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(key1 = pages) {
                    pages?.forEach {
                        val request = ImageRequest.Builder(context)
                            .data(it)
                            .build()
                        context.imageLoader.enqueue(request)
                    }
                }

                ChapterView(
                    title = if (page != null && pages != null) "${pages!!.indexOf(page!!) + 1} / ${pages!!.count()}" else "Loading...",
                    currentPageUrl = page,
                    chapterTapAreaSize = viewModel.chapterTapAreaSize,
                    tappedRightSide = { viewModel.nextPage() },
                    tappedLeftSide = { viewModel.prevPage() },
                    callClose = {
                        lifecycleScope.launch {
                            viewModel.markAsRead()
                            finish()
                        }
                    }
                )
            }
        }
        viewModel.parseIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            viewModel.parseIntent(intent)
        }
    }

    companion object {
        const val EXTRA_UICHAPTER = "EXTRA_UICHAPTER"
        const val EXTRA_UIMANGA = "EXTRA_UIMANGA"

        fun newIntent(context: Context, chapter: UIChapter, manga: UIManga): Intent {
            val intent = Intent(context, ChapterActivity::class.java)
            intent.putExtra(EXTRA_UIMANGA, manga)
            intent.putExtra(EXTRA_UICHAPTER, chapter)
            return intent
        }
    }
}

@Composable
private fun ChapterTapArea(chapterTapAreaSize: Dp, modifier: Modifier) {
    Box(modifier = modifier
        .fillMaxHeight()
        .width(chapterTapAreaSize)
    )
}

@Composable
private fun ChapterView(
    title: String,
    currentPageUrl: String?,
    chapterTapAreaSize: Dp,
    tappedRightSide: () -> Unit,
    tappedLeftSide: () -> Unit,
    callClose: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CloseBanner(title, callClose = callClose)
            if (currentPageUrl == null) {
                LoadingScreen(refreshStatus = null)
            } else {
                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .pointerInput(Unit) {
                            forEachGesture {
                                awaitPointerEventScope {
                                    awaitFirstDown()
                                    do {
                                        val event = awaitPointerEvent()
                                        scale = maxOf(1f, scale * event.calculateZoom())
                                        val offsetChange = event.calculatePan()
                                        offset += offsetChange
                                        if (scale == 1f) {
                                            offset = Offset.Zero
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                        }
                ) {
                    ChapterTapArea(chapterTapAreaSize, modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { tappedLeftSide() }
                    )

                    ChapterTapArea(chapterTapAreaSize, modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { tappedRightSide() }
                    )

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentPageUrl)
                            .crossfade(true)
                            .build(),
                        loading = {
                            LoadingScreen(refreshStatus = null)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentDescription = "Manga page"
                    )
                }
            }
        }
    }
}