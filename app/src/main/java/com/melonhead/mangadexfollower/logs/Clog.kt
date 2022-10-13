package com.melonhead.mangadexfollower.logs

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import io.ktor.client.network.sockets.*
import java.io.EOFException
import java.io.IOException

object Clog {
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

    fun e(message: String, exception: Exception) {
        i(message)
        when (exception) {
            is ConnectTimeoutException -> return
        }
        Firebase.crashlytics.recordException(exception)
    }
}