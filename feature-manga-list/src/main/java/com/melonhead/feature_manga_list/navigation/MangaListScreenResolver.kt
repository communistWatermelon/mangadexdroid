package com.melonhead.feature_manga_list.navigation

import androidx.compose.runtime.Composable
import com.melonhead.feature_manga_list.ui.scenes.MangaListScreen
import com.melonhead.lib_navigation.keys.ScreenKey
import com.melonhead.lib_navigation.resolvers.ScreenResolver

class MangaListScreenResolver: ScreenResolver<ScreenKey.MangaListScreen> {
    @Composable
    override fun ComposeWithKey(key: ScreenKey.MangaListScreen) {
        MangaListScreen(
            buildVersionName = key.buildVersionName,
            buildVersionCode = key.buildVersionCode,
        )
    }
}
