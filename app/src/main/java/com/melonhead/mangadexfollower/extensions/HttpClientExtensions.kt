package com.melonhead.mangadexfollower.extensions

import com.melonhead.mangadexfollower.App
import com.melonhead.mangadexfollower.logs.Clog
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.statement.*

suspend inline fun <reified T> HttpClient.catching(logMessage: String, function: HttpClient.() -> HttpResponse): T? {
    Clog.i(logMessage)
    var response: HttpResponse? = null
    return try {
        response = function()
        response.body()
    } catch (e: Exception) {
        if (response?.status?.value == 401) {
            App.authFailed()
        } else {
            Clog.e("$logMessage: ${response?.bodyAsText() ?: ""}", e)
        }
        null
    }
}