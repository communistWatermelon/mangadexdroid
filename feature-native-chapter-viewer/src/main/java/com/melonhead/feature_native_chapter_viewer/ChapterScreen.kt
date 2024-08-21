package com.melonhead.feature_native_chapter_viewer

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.melonhead.lib_core.scenes.CloseBanner
import com.melonhead.lib_core.scenes.LoadingScreen
import com.melonhead.lib_logging.Clog
import java.lang.Integer.min

@Composable
internal fun ChapterScreen(
    currentPage: String?,
    allPages: List<String>?,
    chapterTapAreaSize: Dp,
    onCompletedChapter: () -> Unit,
    nextPage: () -> Unit,
    prevPage: () -> Unit,
) {
    if (currentPage == null || allPages == null) {
        LoadingChapterView(onDoneTapped = { })
    } else {
        val context = LocalContext.current
        val (width, height) = getWidthHeight()

        fun preloadImage(url: String, currentPageIndex: Int) {
            val request = url.preloadImageRequest(currentPageIndex, context, width, height)
            context.imageLoader.enqueue(request)
        }

        LaunchedEffect(key1 = allPages) {
            val preloadPages = 1
            Clog.i("First page - Preloading pages 1 - ${1 + preloadPages}")
            allPages.slice(min(1, allPages.count() - 1)..min((1 + preloadPages), allPages.count() - 1)).forEach { page ->
                preloadImage(page, allPages.indexOf(page))
            }
        }

        val currentPageIndex = allPages.indexOf(currentPage)
        val totalPages = allPages.count()
        ChapterView(
            title = "${currentPageIndex + 1} / $totalPages",
            currentPageIndex = currentPageIndex,
            currentPageUrl = currentPage,
            chapterTapAreaSize = chapterTapAreaSize,
            tappedRightSide = {
                val nextPreloadIndex = currentPageIndex + 2
                val start = min(nextPreloadIndex, totalPages - 1)
                val end = min(totalPages - 1, nextPreloadIndex + 1)
                Clog.i("Next page - Current Page $currentPageIndex, moving to ${currentPageIndex + 1} Preloading pages $start - $end")
                allPages.slice(start..end).forEach {
                    preloadImage(it, allPages.indexOf(it))
                }
                nextPage()
            },
            tappedLeftSide = { prevPage() },
            onDoneTapped = {
                onCompletedChapter()
            }
        )
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
    onDoneTapped: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CloseBanner("Loading...", onDoneTapped = onDoneTapped)
            LoadingScreen(refreshStatus = null)
        }
    }
}

@Composable
private fun ChapterView(
    title: String,
    currentPageUrl: String,
    currentPageIndex: Int,
    chapterTapAreaSize: Dp,
    tappedRightSide: () -> Unit,
    tappedLeftSide: () -> Unit,
    onDoneTapped: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CloseBanner(title, onDoneTapped = onDoneTapped)
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

                var retryHash by remember { mutableStateOf(false) }
                val (width, height) = getWidthHeight()
                SubcomposeAsyncImage(
                    model = currentPageUrl.preloadImageRequest(pageIndex = currentPageIndex, LocalContext.current, width, height, retryHash) {
                        Clog.i("Retrying due to load failure")
                        retryHash = !retryHash
                    },
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

@Composable
private fun getWidthHeight(): Pair<Int, Int> {
    val configuration = LocalConfiguration.current
    val width = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }.toInt()
    val height = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }.toInt()
    return width to height
}

private fun String.preloadImageRequest(pageIndex: Int, context: Context, width: Int, height: Int, retryHash: Boolean = false, onError: () -> Unit = { } ): ImageRequest {
    return ImageRequest.Builder(context)
        .data(this)
        .size(width = width, height = height)
        .crossfade(true)
        .listener(
            onStart = {
                Clog.i("Image Load start: page $pageIndex")
            },
            onCancel = {
                Clog.i("Image Load cancel: page $pageIndex")
            },
            onSuccess = { _, result ->
                Clog.i("Image Load success: Source ${result.dataSource.name}, page $pageIndex")
            },
            onError = { _, result ->
                Clog.i("Image Load failed: page $pageIndex")
                Clog.e("Image Load failed", result.throwable)
                onError()
            }
        )
        .setParameter("retry_hash", retryHash)
        .build()
}
