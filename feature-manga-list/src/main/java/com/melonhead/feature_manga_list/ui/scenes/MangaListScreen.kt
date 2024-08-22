package com.melonhead.feature_manga_list.ui.scenes

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.melonhead.data_shared.models.ui.None
import com.melonhead.data_shared.models.ui.UIChapter
import com.melonhead.data_shared.models.ui.UIManga
import com.melonhead.lib_core.scenes.LoadingScreen
import com.melonhead.feature_manga_list.ui.scenes.dialogs.MarkChapterReadDialog
import com.melonhead.feature_manga_list.ui.scenes.dialogs.ShowMangaDescriptionDialog
import com.melonhead.feature_manga_list.ui.scenes.dialogs.TitleChangeDialog
import com.melonhead.feature_manga_list.ui.scenes.dialogs.ToggleRenderTypeDialog
import com.melonhead.feature_manga_list.viewmodels.MangaListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun MangaListScreen(
    viewModel: MangaListViewModel = koinViewModel(),
    buildVersionName: String,
    buildVersionCode: String,
) {
    var chapterReadStatusDialog by remember { mutableStateOf<Pair<UIManga, UIChapter>?>(null) }
    MarkChapterReadDialog(
        chapterReadStatusDialog,
        onToggleChapterRead = { uiManga, chapter -> viewModel.toggleChapterRead(uiManga, chapter) },
        onDismissed = { chapterReadStatusDialog = null }
    )

    var mangaWebviewToggleDialog by remember { mutableStateOf<UIManga?>(null) }
    ToggleRenderTypeDialog(
        mangaWebviewToggleDialog,
        onRenderTypeToggled = { uiManga -> viewModel.toggleMangaWebview(uiManga) },
        onDismissed = { mangaWebviewToggleDialog = null }
    )

    var showTitleChangeDialogForManga by remember { mutableStateOf<UIManga?>(null) }
    TitleChangeDialog(
        showTitleChangeDialogForManga,
        onChangeMangaTitle = { uiManga, title -> viewModel.setMangaTitle(uiManga, title) },
        onDismissed = { showTitleChangeDialogForManga = null }
    )

    var showDescriptionDialogForManga by remember { mutableStateOf<UIManga?>(null) }
    ShowMangaDescriptionDialog(
        showDescriptionDialogForManga,
        onDismissed = { showDescriptionDialogForManga = null }
    )

    val isRefreshing = rememberSwipeRefreshState(isRefreshing = false)
    var justPulledRefresh by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val manga by viewModel.manga.observeAsState(listOf())
    val refreshStatus by viewModel.refreshStatus.observeAsState(None)
    val refreshText by viewModel.refreshText.observeAsState("")
    val readMangaCount = viewModel.readMangaCount

    if (manga.isEmpty()) {
        LoadingScreen(refreshStatus)
    } else {

        val itemState = remember(manga, refreshStatus) {
            val items = mutableListOf<Any>()
            manga.forEach { manga ->
                items.add(manga)
                manga.chapters.filter { it.read != true }.forEach {
                    items.add(it to manga)
                }
                manga.chapters.filter { it.read == true }.take(readMangaCount).forEach {
                    items.add(it to manga)
                }
            }
            items.toList()
        }
        LaunchedEffect(refreshStatus) { justPulledRefresh = false }

        Column {
            AnimatedVisibility(visible = refreshStatus !is None || isRefreshing.isRefreshing || justPulledRefresh) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceTint)
                        .padding(8.dp)
                        .clickable {
                            Toast
                                .makeText(
                                    context,
                                    "Version ${buildVersionName}.${buildVersionCode}",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(12.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        text = refreshStatus.text,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            SwipeRefresh(
                state = isRefreshing,
                onRefresh = {
                    justPulledRefresh = true
                    viewModel.refreshContent()
                },
                swipeEnabled = (refreshStatus is None) && !isRefreshing.isRefreshing && !justPulledRefresh
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        AnimatedVisibility(visible = refreshStatus is None && !isRefreshing.isRefreshing) {
                            Text(text = "Last Refresh: $refreshText",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally)
                                    .clickable {
                                        Toast
                                            .makeText(
                                                context,
                                                "Version ${buildVersionName}.${buildVersionCode}",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    .padding(bottom = 12.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center)
                        }
                    }

                    items(itemState, {
                        when {
                            it is UIManga -> it.id
                            it is Pair<*, *> && it.first is UIChapter -> (it.first as UIChapter).id
                            else -> it.hashCode()
                        }
                    }) {
                        if (it is UIManga) {
                            MangaCoverListItem(
                                modifier = Modifier.padding(top = if (itemState.first() == it) 0.dp else 12.dp),
                                uiManga = it,
                                onLongPress = { mangaWebviewToggleDialog = it },
                                onTapped = { showDescriptionDialogForManga = it },
                                onTitleLongPress = {
                                    showTitleChangeDialogForManga = it
                                }
                            )
                        }
                        if (it is Pair<*, *> && it.first is UIChapter) {
                            ChapterListItem(
                                modifier = Modifier.padding(bottom = 12.dp),
                                uiChapter = it.first as UIChapter,
                                uiManga = it.second as UIManga,
                                refreshStatus = refreshStatus,
                                onChapterClicked = { uiManga, uiChapter ->
                                    viewModel.onChapterClicked(context, uiManga, uiChapter)
                                },
                                onChapterLongPressed = { uiManga, uiChapter ->
                                    chapterReadStatusDialog = uiManga to uiChapter
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
