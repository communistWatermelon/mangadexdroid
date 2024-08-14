package com.melonhead.feature_native_chapter_viewer

import android.content.Context
import android.content.Intent
import com.melonhead.data_core_manga_ui.UIChapter
import com.melonhead.data_core_manga_ui.UIManga
import com.melonhead.lib_navigation.keys.ActivityKey
import com.melonhead.lib_navigation.resolvers.ActivityResolver

class NativeChapterViewerActivityResolver internal constructor(): ActivityResolver<ActivityKey.ChapterActivity> {
    override fun intentForKey(context: Context, key: ActivityKey.ChapterActivity): Intent {
        return ChapterActivity.newIntent(
            context,
            key.params.getParcelable(ActivityKey.ChapterActivity.PARAM_CHAPTER)!!,
            key.params.getParcelable(ActivityKey.ChapterActivity.PARAM_MANGA)!!,
        )
    }
}
