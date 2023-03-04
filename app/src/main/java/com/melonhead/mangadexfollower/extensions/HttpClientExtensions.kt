package com.melonhead.mangadexfollower.extensions

import com.melonhead.mangadexfollower.App
import com.melonhead.mangadexfollower.logs.Clog
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlin.random.Random

suspend inline fun <reified T> HttpClient.catching(logMessage: String, function: HttpClient.() -> HttpResponse): T? {
    Clog.i(logMessage)
    var response: HttpResponse? = null
    return try {
        response = function()
        response.body()
    } catch (e: Exception) {
        if (response?.status?.value == 401) {
            // note: auth randomly seems to fail, even if the token is valid. we can limit that by only sometimes failing auth
            // hopefully this is fixed with oauth
            if (Random.nextInt(100) < 20) {
                App.authFailed()
            }
        } else {
            Clog.e("$logMessage: ${response?.bodyAsText() ?: ""}", e)
        }
        null
    }
}