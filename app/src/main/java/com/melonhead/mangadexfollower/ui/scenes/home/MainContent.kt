package com.melonhead.mangadexfollower.ui.scenes.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.melonhead.mangadexfollower.models.ui.LoginStatus
import com.melonhead.mangadexfollower.models.ui.None
import com.melonhead.mangadexfollower.ui.scenes.shared.LoadingScreen
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel

@Composable
internal fun MainContent(
    viewModel: MainViewModel,
) {
    val loginStatus by viewModel.loginStatus.observeAsState()
    val manga by viewModel.manga.observeAsState(listOf())
    val refreshStatus by viewModel.refreshStatus.observeAsState(None)
    val refreshText by viewModel.refreshText.observeAsState("")
    val context = LocalContext.current

    when (loginStatus) {
        LoginStatus.LoggedIn -> {
            if (manga.isEmpty()) {
                LoadingScreen(refreshStatus)
            } else {
                HomeList(
                    manga,
                    readMangaCount = viewModel.readMangaCount,
                    refreshText = refreshText,
                    refreshStatus = refreshStatus,
                    onChapterClicked = { uiManga, chapter -> viewModel.onChapterClicked(context, uiManga, chapter) },
                    onToggleChapterRead = { uiManga, uiChapter -> viewModel.toggleChapterRead(uiManga, uiChapter) },
                    onSwipeRefresh = { viewModel.refreshContent() },
                    onToggleMangaRenderType = { uiManga -> viewModel.toggleMangaWebview(uiManga) },
                    onChangeMangaTitle = { uiManga, title ->  viewModel.setMangaTitle(uiManga, title) },
                )
            }
        }
        LoginStatus.LoggedOut -> LoginScreen { username, password -> viewModel.authenticate(username, password) }
        LoginStatus.LoggingIn, null -> LoadingScreen(null)
    }
}
