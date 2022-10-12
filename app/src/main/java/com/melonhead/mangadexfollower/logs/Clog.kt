package com.melonhead.mangadexfollower.logs

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.melonhead.mangadexfollower.repositories.AuthRepository
import io.ktor.client.network.sockets.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Clog: KoinComponent {
    private val authRepository: AuthRepository by inject()

    fun d(message: String) {
        Log.d(null, message)
        Firebase.crashlytics.log(message)
    }

    fun i(message: String) {
        Log.i(null, message)
        Firebase.crashlytics.log(message)
    }
    
    fun w(message: String) {
        Log.w(null, message)
        Firebase.crashlytics.log(message)
    }

    suspend fun e(message: String, exception: Exception) {
        i(message)
        if (message.contains("User not found", ignoreCase = true) && message.contains("unauthorized_http_exception", ignoreCase = true)) {
            authRepository.checkCurrentAuthentication()
            return
        }
        when (exception) {
            is ConnectTimeoutException -> return
        }
        Firebase.crashlytics.recordException(exception)
    }
}