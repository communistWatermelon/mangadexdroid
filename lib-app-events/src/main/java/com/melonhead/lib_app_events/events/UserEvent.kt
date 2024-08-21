package com.melonhead.lib_app_events.events

import android.content.Context
import com.melonhead.lib_core.models.UIChapter
import com.melonhead.lib_core.models.UIManga

sealed class UserEvent: AppEvent {
    data object RefreshManga: UserEvent()
    data class SetUseWebView(val manga: UIManga, val useWebView: Boolean): UserEvent()
    data class SetMarkChapterRead(val chapter: UIChapter, val manga: UIManga, val read: Boolean): UserEvent()
    data class UpdateChosenMangaTitle(val manga: UIManga, val title: String): UserEvent()
    data class OpenedNotification(val context: Context, val manga: UIManga, val chapter: UIChapter): UserEvent()
}
