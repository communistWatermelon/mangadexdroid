package com.melonhead.lib_app_events.events

sealed class SystemLogicEvents: AppEvent {
    data class PromptMangaRating(val mangaId: String): SystemLogicEvents()
}
