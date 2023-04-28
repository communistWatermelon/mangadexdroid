package com.melonhead.mangadexfollower

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.melonhead.mangadexfollower.di.appModule
import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.work_manager.RefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class App: Application() {
    private val mangaRepository: MangaRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val externalScope: CoroutineScope by inject()
    private val appDataService: AppDataService by inject()

    var inForeground = false
        private set
    override fun onCreate() {
        super.onCreate()

        instance = this

        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@App)
            // Load modules
            modules(appModule)
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                inForeground = true
                externalScope.launch { mangaRepository.forceRefresh() }
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                inForeground = false
                Clog.i("onStop: Creating background task")
                val refreshWorkRequest = PeriodicWorkRequestBuilder<RefreshWorker>(15.minutes.toJavaDuration()).build()
                WorkManager.getInstance(this@App).enqueueUniquePeriodicWork("refresh-task", ExistingPeriodicWorkPolicy.KEEP, refreshWorkRequest)
            }
        })
    }

    companion object {
        private lateinit var instance: App
        suspend fun authFailed() {
            instance.authRepository.refreshToken(logoutOnFail = true)
        }
    }
}