package com.melonhead.mangadexfollower

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.melonhead.lib_app_context.AppContext
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.AppLifecycleEvent
import com.melonhead.lib_app_events.events.AuthenticationEvent
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.extensions.error401Callback
import com.melonhead.mangadexfollower.di.AppModule
import com.melonhead.mangadexfollower.work_manager.RefreshWorker
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class App: Application() {
    private val appEventsRepository: AppEventsRepository by inject()
    private val appNavigationMap: AppNavigationMap by inject()
    private val appContext: AppContext by inject()

    override fun onCreate() {
        super.onCreate()

        instance = this

        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@App)
            // Load modules
            modules(AppModule)
        }

        // force navigation initialization
        appNavigationMap

        error401Callback = {
            appEventsRepository.postEvent(AuthenticationEvent.RefreshToken(logoutOnFail = true))
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                appContext.isInForeground = true
                appEventsRepository.postEvent(AppLifecycleEvent.AppForegrounded)
                Clog.i("Creating background task")
                val refreshWorkRequest = PeriodicWorkRequestBuilder<RefreshWorker>(15.minutes.toJavaDuration()).build()
                WorkManager.getInstance(this@App).enqueueUniquePeriodicWork("refresh-task", ExistingPeriodicWorkPolicy.KEEP, refreshWorkRequest)
            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                appContext.isInForeground = false
            }
        })
    }

    companion object {
        private lateinit var instance: App
    }
}
