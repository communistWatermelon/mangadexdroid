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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.ui.scenes.shared.CloseBanner
import com.melonhead.mangadexfollower.ui.scenes.shared.LoadingScreen
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import com.melonhead.mangadexfollower.ui.viewmodels.ChapterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Integer.min

@Composable
private fun getWidthHeight(): Pair<Int, Int> {
    val configuration = LocalConfiguration.current
    val width = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }.toInt()
    val height = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }.toInt()
    return width to height
}

private fun String.preloadImageRequest(context: Context, width: Int, height: Int): ImageRequest {
    return ImageRequest.Builder(context)
        .data(this)
        .size(width = width, height = height)
        .crossfade(true)
        .listener(
            onStart = { request ->
                Clog.i("Image Load start: URL ${request.data}")
            },
            onCancel = { request ->
                Clog.i("Image Load cancel: URL ${request.data}")
            },
            onSuccess = { request, result ->
                Clog.i("Image Load success: Source ${result.dataSource.name}, URL ${request.data}")
            },
            onError = { request, result ->
                Clog.i("Image Load failed: URL ${request.data}")
                Clog.e("Image Load failed", result.throwable)
            }
        )
        .build()
}

class ChapterActivity: ComponentActivity() {
    private val viewModel by viewModel<ChapterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MangadexFollowerTheme {
                val page by viewModel.currentPage.collectAsState(initial = null)
                val pages by viewModel.chapterData.collectAsState()
                val context = LocalContext.current
                val (width, height) = getWidthHeight()

                fun preloadImage(url: String) {
                    val request = url.preloadImageRequest(context, width, height)
                    context.imageLoader.enqueue(request)
                }

                LaunchedEffect(key1 = pages) {
                    val preloadPages = 3
                    pages?.take(preloadPages)?.forEach {
                        Clog.d("Initial preload - $preloadPages pages")
                        preloadImage(it)
                    }
                }

                val allPages = pages
                val currentPage = page
                if (currentPage == null || allPages == null) {
                    LoadingChapterView {
                        finish()
                    }
                } else {
                    val currentPageIndex = allPages.indexOf(currentPage)
                    val totalPages = allPages.count()
                    ChapterView(
                        title = "${currentPageIndex + 1} / $totalPages",
                        currentPageUrl = currentPage,
                        chapterTapAreaSize = viewModel.chapterTapAreaSize,
                        tappedRightSide = {
                            val nextPreloadIndex = currentPageIndex + 3
                            val start = min(nextPreloadIndex, totalPages - 1)
                            val end = min(totalPages - 1, nextPreloadIndex + 2)
                            Clog.i("Next page - Preloading pages $start - $end")
                            allPages.slice(start..end).forEach { preloadImage(it) }
                            viewModel.nextPage()
                        },
                        tappedLeftSide = { viewModel.prevPage() },
                        callClose = {
                            lifecycleScope.launch(Dispatchers.IO) {
                                viewModel.markAsRead()
                                finish()
                            }
                        }
                    )
                }

            }
        }
        viewModel.parseIntent(this, intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            viewModel.parseIntent(this, intent)
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
private fun LoadingChapterView(
    callClose: () -> Unit

) {
    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CloseBanner("Loading...", callClose = callClose)
            LoadingScreen(refreshStatus = null)
        }
    }
}

@Composable
private fun ChapterView(
    title: String,
    currentPageUrl: String,
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

                val (width, height) = getWidthHeight()
                SubcomposeAsyncImage(
                    model = currentPageUrl.preloadImageRequest(LocalContext.current, width, height),
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