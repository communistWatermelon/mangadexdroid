package com.melonhead.lib_networking.extensions

import com.melonhead.lib_logging.Clog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import java.io.File
import kotlin.random.Random

// this is bad, but we need to refactor out an event system to avoid it
var error401Callback: (suspend () -> Unit)? = null

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
                error401Callback?.invoke()
            }
            Clog.w("$logMessage: ${response.bodyAsText()}")
        } else {
            Clog.e("$logMessage: ${response?.bodyAsText() ?: ""}", e)
        }
        null
    }
}

suspend inline fun HttpClient.catchingSuccess(logMessage: String, function: HttpClient.() -> HttpResponse): Boolean {
    Clog.i(logMessage)
    var response: HttpResponse? = null
    return try {
        response = function()
        true
    } catch (e: Exception) {
        @Suppress("KotlinConstantConditions")
        if (response?.status?.value == 401) {
            // note: auth randomly seems to fail, even if the token is valid. we can limit that by only sometimes failing auth
            // hopefully this is fixed with oauth
            if (Random.nextInt(100) < 20) {
                error401Callback?.invoke()
            }
        } else {
            Clog.e("$logMessage: ${response?.bodyAsText() ?: ""}", e)
        }
        false
    }
}

suspend fun HttpClient.downloadFile(outputFile: File, url: String): Boolean {
    return prepareGet(url).execute { httpResponse ->
        val channel: ByteReadChannel = httpResponse.body()
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
            while (!packet.isEmpty) {
                val bytes = packet.readBytes()
                outputFile.appendBytes(bytes)
            }
        }
        return@execute httpResponse.contentLength() == outputFile.length()
    }
}
