package com.melonhead.mangadexfollower

import com.melonhead.feature_native_chapter_viewer.NativeChapterViewerActivityResolver
import com.melonhead.feature_webview_chapter_viewer.WebViewChapterViewerActivityResolver
import com.melonhead.lib_navigation.keys.ActivityKey
import com.melonhead.lib_navigation.resolvers.ResolverMap
import com.melonhead.mangadexfollower.navigation.MainActivityResolver

class AppNavigationMap(
    resolverMap: ResolverMap,

    mainActivityResolver: MainActivityResolver,
    nativeChapterViewerActivityResolver: NativeChapterViewerActivityResolver,
    webViewActivityResolver: WebViewChapterViewerActivityResolver,
) {
    init {
        resolverMap.registerResolver(
            ActivityKey.WebViewActivity::class.java, webViewActivityResolver
        )
        resolverMap.registerResolver(
            ActivityKey.ChapterActivity::class.java, nativeChapterViewerActivityResolver
        )
        resolverMap.registerResolver(
            ActivityKey.MainActivity::class.java, mainActivityResolver
        )
    }
}
