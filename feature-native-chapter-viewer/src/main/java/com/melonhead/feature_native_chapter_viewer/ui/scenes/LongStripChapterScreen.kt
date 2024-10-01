package com.melonhead.feature_native_chapter_viewer.ui.scenes

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.melonhead.lib_core.scenes.CloseBanner
import com.melonhead.lib_core.scenes.LoadingScreen
import com.melonhead.lib_core.theme.MangadexFollowerTheme
import com.melonhead.lib_logging.Clog
import java.lang.Integer.min

private val LazyListState.isLastItemVisible: Boolean
    get() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1

@Composable
internal fun LongStripChapterScreen(
    allPages: List<String>?,
    onCompletedChapter: () -> Unit
) {
    if (allPages == null) {
        LoadingChapterView(onDoneTapped = { })
    } else {
        val context = LocalContext.current

        fun preloadImage(url: String, currentPageIndex: Int) {
            val request = url.preloadImageRequest(currentPageIndex, context)
            context.imageLoader.enqueue(request)
        }

        LaunchedEffect(key1 = allPages) {
            val preloadPages = 1
            Clog.i("First page - Preloading pages 1 - ${1 + preloadPages}")
            allPages.slice(min(1, allPages.count() - 1)..min((1 + preloadPages), allPages.count() - 1)).forEach { page ->
                preloadImage(page, allPages.indexOf(page))
            }
        }

        Column {
            val state = rememberLazyListState()
            val pageIndex by remember {
                derivedStateOf {
                    if (state.isLastItemVisible) {
                        allPages.count()
                    } else {
                        state.firstVisibleItemIndex + 1
                    }
                }
            }

            CloseBanner(
                "$pageIndex / ${allPages.count()}",
                onDoneTapped = onCompletedChapter
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                state = state,
            ) {
                items(allPages.count()) { index ->
                    ChapterView(
                        currentPageUrl = allPages[index],
                        currentPageIndex = index
                    )
                }
            }
        }


    }
}


@Composable
private fun ChapterView(
    currentPageUrl: String,
    currentPageIndex: Int,
) {
    Surface(modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            var retryHash by remember { mutableStateOf(false) }
            SubcomposeAsyncImage(
                contentScale = ContentScale.FillWidth,
                model = currentPageUrl.preloadImageRequest(
                    pageIndex = currentPageIndex,
                    LocalContext.current,
                    retryHash
                ) {
                    Clog.i("Retrying due to load failure")
                    retryHash = !retryHash
                },
                loading = {
                    LoadingScreen(refreshStatus = null)
                },
                modifier = Modifier
                    .fillMaxSize(),
                contentDescription = "Manga page"
            )

        }
    }
}

private fun String.preloadImageRequest(
    pageIndex: Int,
    context: Context,
    retryHash: Boolean = false,
    onError: () -> Unit = { }
): ImageRequest {
    return ImageRequest.Builder(context)
        .data(this)
        .size(Size.ORIGINAL)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LongStripPreview() {
    MangadexFollowerTheme {
        LongStripChapterScreen(listOf()) {

        }
    }
}
