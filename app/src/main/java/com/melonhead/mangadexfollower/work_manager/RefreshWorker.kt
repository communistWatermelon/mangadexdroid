package com.melonhead.mangadexfollower.work_manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.melonhead.mangadexfollower.repositories.MangaRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RefreshWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams), KoinComponent {
    private val mangaRepository: MangaRepository by inject()

    override suspend fun doWork(): Result {
        mangaRepository.forceRefresh()
        return Result.success()
    }
}