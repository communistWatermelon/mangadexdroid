package com.melonhead.lib_app_events

import com.melonhead.lib_app_events.events.AppEvent
import com.melonhead.lib_logging.Clog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface AppEventsRepository {
    val events: Flow<AppEvent>
    fun postEvent(appEvent: AppEvent)
}

internal class AppEventsRepositoryImpl: AppEventsRepository {
    private val mutableEvents = MutableSharedFlow<AppEvent>(1)
    override val events: Flow<AppEvent> = mutableEvents.asSharedFlow()

    override fun postEvent(appEvent: AppEvent) {
        val result = mutableEvents.tryEmit(appEvent)
        if (!result) {
            Clog.w("Failed to post event: $appEvent")
        }
    }
}
