package com.melonhead.mangadexfollower.work_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.melonhead.feature_manga_list.usecases.RefreshMangaUseCase
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_logging.Clog
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RefreshWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams), KoinComponent {
    @Suppress("DEPRECATION") // Necessary to await the result of the refresh
    private val refreshMangaUseCase: RefreshMangaUseCase by inject()

    override suspend fun doWork(): Result {
        refreshMangaUseCase.invoke()
        return Result.success()
    }
}
