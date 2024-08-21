package com.melonhead.mangadexfollower

import com.melonhead.feature_authentication.navigation.LoginScreenResolver
import com.melonhead.feature_manga_list.navigation.MangaListScreenResolver
import com.melonhead.feature_native_chapter_viewer.navigation.NativeChapterViewerActivityResolver
import com.melonhead.feature_webview_chapter_viewer.navigation.WebViewChapterViewerActivityResolver
import com.melonhead.lib_navigation.keys.ActivityKey
import com.melonhead.lib_navigation.keys.ScreenKey
import com.melonhead.lib_navigation.resolvers.ResolverMap
import com.melonhead.mangadexfollower.navigation.MainActivityResolver

class AppNavigationMap(
    resolverMap: ResolverMap,

    mainActivityResolver: MainActivityResolver,
    mangaListScreenResolver: MangaListScreenResolver,
    nativeChapterViewerActivityResolver: NativeChapterViewerActivityResolver,
    webViewActivityResolver: WebViewChapterViewerActivityResolver,

    loginScreenResolver: LoginScreenResolver,
) {
    init {
        // activities
        resolverMap.registerResolver(ActivityKey.WebViewActivity::class.java, webViewActivityResolver)
        resolverMap.registerResolver(ActivityKey.ChapterActivity::class.java, nativeChapterViewerActivityResolver)
        resolverMap.registerResolver(ActivityKey.MainActivity::class.java, mainActivityResolver)

        // screens
        resolverMap.registerResolver(ScreenKey.LoginScreen::class.java, loginScreenResolver)
        resolverMap.registerResolver(ScreenKey.MangaListScreen::class.java, mangaListScreenResolver)
    }
}
