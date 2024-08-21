package com.melonhead.feature_webview_chapter_viewer.navigation

import android.content.Context
import android.content.Intent
import com.melonhead.feature_webview_chapter_viewer.WebViewActivity
import com.melonhead.lib_navigation.keys.ActivityKey
import com.melonhead.lib_navigation.resolvers.ActivityResolver

class WebViewChapterViewerActivityResolver internal constructor():
    ActivityResolver<ActivityKey.WebViewActivity> {
    override fun intentForKey(context: Context, key: ActivityKey.WebViewActivity): Intent {
        return WebViewActivity.newIntent(
            context,
            key.params.getParcelable(ActivityKey.WebViewActivity.PARAM_CHAPTER)!!,
            key.params.getParcelable(ActivityKey.WebViewActivity.PARAM_MANGA)!!,
        )
    }
}
