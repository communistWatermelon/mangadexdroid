package com.melonhead.lib_app_events.events

sealed class UserEvent: AppEvent {
    data object RefreshManga: UserEvent()
}
