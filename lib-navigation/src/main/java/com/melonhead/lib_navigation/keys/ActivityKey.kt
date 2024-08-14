package com.melonhead.lib_navigation.keys

import android.os.Bundle

sealed class ActivityKey {
    data object MainActivity : ActivityKey()
    data class WebViewActivity(val params: Bundle) : ActivityKey() {
        companion object {
            const val PARAM_MANGA = "manga"
            const val PARAM_CHAPTER = "chapter"
        }
    }
    data class ChapterActivity(val params: Bundle) : ActivityKey() {
        companion object {
            const val PARAM_MANGA = "manga"
            const val PARAM_CHAPTER = "chapter"
        }
    }
}
