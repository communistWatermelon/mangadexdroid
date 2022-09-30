package com.melonhead.mangadexfollower

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.melonhead.mangadexfollower.di.appModule
import com.melonhead.mangadexfollower.work_manager.RefreshWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@App)
            // Load modules
            modules(appModule)
        }

        val refreshWorkRequest = PeriodicWorkRequestBuilder<RefreshWorker>(15.minutes.toJavaDuration()).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("refresh-task", ExistingPeriodicWorkPolicy.KEEP, refreshWorkRequest)
    }
}