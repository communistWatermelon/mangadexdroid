package com.melonhead.lib_app_events.events

import com.melonhead.core_ui.models.UIChapter
import com.melonhead.core_ui.models.UIManga

sealed class UserEvent: AppEvent {
    data object RefreshManga: UserEvent()
    data class SetUseWebView(val manga: UIManga, val useWebView: Boolean): UserEvent()
    data class SetMarkChapterRead(val chapter: UIChapter, val manga: UIManga, val read: Boolean): UserEvent()
    data class UpdateChosenMangaTitle(val manga: UIManga, val title: String): UserEvent()
}
