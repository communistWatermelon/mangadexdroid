package com.melonhead.lib_app_events.events

import android.content.Context
import com.melonhead.lib_core.models.UIChapter
import com.melonhead.lib_core.models.UIManga

sealed class UserEvent: AppEvent {
    data object RefreshManga: UserEvent()
    data class SetUseWebView(val mangaId: String, val useWebView: Boolean): UserEvent()
    data class SetMarkChapterRead(val chapterId: String, val mangaId: String, val read: Boolean): UserEvent()
    data class UpdateChosenMangaTitle(val mangaId: String, val title: String): UserEvent()
    data class OpenedNotification(val context: Context, val manga: UIManga, val chapter: UIChapter): UserEvent()
}
