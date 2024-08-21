package com.melonhead.lib_navigation.keys

import com.melonhead.core_ui.models.MangaRefreshStatus
import com.melonhead.core_ui.models.UIChapter
import com.melonhead.core_ui.models.UIManga
import kotlinx.coroutines.Job

sealed class ScreenKey {
    data class LoginScreen(val onLoginTapped: (email: String, password: String) -> Unit) : ScreenKey()
    data class MangaListScreen(
        val buildVersionName: String,
        val buildVersionCode: String,
        val manga: List<UIManga>,
        val readMangaCount: Int,
        val refreshText: String,
        val refreshStatus: MangaRefreshStatus,
        val onChapterClicked: (UIManga, UIChapter) -> Unit,
        val onToggleChapterRead: (UIManga, UIChapter) -> Unit,
        val onSwipeRefresh: () -> Unit,
        val onToggleMangaRenderType: (UIManga) -> Unit,
        val onChangeMangaTitle: (UIManga, String) -> Unit
    ) : ScreenKey()
}
