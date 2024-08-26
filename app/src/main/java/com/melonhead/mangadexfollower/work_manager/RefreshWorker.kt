package com.melonhead.mangadexfollower.work_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_logging.Clog
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RefreshWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams), KoinComponent {
    private val appEventsRepository: AppEventsRepository by inject()

    override suspend fun doWork(): Result {
        return if (appEventsRepository.postEvent(UserEvent.RefreshManga)) {
            Result.success()
        } else {
            Clog.e("RefreshWorker: Failed to post RefreshManga event", RuntimeException("RefreshWorker: Failed to post RefreshManga event"))
            Result.failure()
        }
    }
}
