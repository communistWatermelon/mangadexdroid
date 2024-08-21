package com.melonhead.feature_native_chapter_viewer.navigation

import android.content.Context
import android.content.Intent
import com.melonhead.feature_native_chapter_viewer.ChapterActivity
import com.melonhead.lib_navigation.keys.ActivityKey
import com.melonhead.lib_navigation.resolvers.ActivityResolver

class NativeChapterViewerActivityResolver internal constructor(): ActivityResolver<ActivityKey.ChapterActivity> {
    override fun intentForKey(context: Context, key: ActivityKey.ChapterActivity): Intent {
        return ChapterActivity.newIntent(
            context,
            key.params.getParcelable(ActivityKey.ChapterActivity.PARAM_CHAPTER)!!,
            key.params.getParcelable(ActivityKey.ChapterActivity.PARAM_MANGA)!!,
            key.params.getStringArray(ActivityKey.ChapterActivity.PARAM_CHAPTER_DATA)!!.toList(),
        )
    }
}
