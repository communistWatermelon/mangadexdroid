package com.melonhead.mangadexfollower.work_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.UserEvent
import kotlinx.coroutines.future.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.CompletableFuture

class RefreshWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams), KoinComponent {
    private val appEventsRepository: AppEventsRepository by inject()

    override suspend fun doWork(): Result {
        val refreshCompletion = CompletableFuture<Unit>()
        appEventsRepository.postEvent(UserEvent.RefreshManga(refreshCompletion))
        refreshCompletion.await()
        return Result.success()
    }
}
