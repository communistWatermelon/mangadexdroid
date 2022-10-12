package com.melonhead.mangadexfollower.logs

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

object Clog {
    inline fun d(tag: String, message: String) {
        Log.d(tag, message)
        Firebase.crashlytics.log("$tag: $message")
    }

    inline fun i(tag: String, message: String) {
        Log.d(tag, message)
        Firebase.crashlytics.log("$tag: $message")
    }
    
    inline fun w(tag: String, message: String) {
        Log.d(tag, message)
        Firebase.crashlytics.log("$tag: $message")
    }
}