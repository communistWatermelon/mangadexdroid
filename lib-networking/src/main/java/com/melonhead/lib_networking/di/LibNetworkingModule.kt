package com.melonhead.lib_networking.di

import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.ratelimit.RateLimit
import com.melonhead.lib_networking.ratelimit.impl.default
import com.melonhead.lib_networking.ratelimit.impl.rate
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import kotlin.time.DurationUnit

val LibNetworkingModule = module {
    single {
        HttpClient(CIO) {
            install(RateLimit) {
                // globally set a 5 permit per 1 second rate-limiting
                default().rate(5, 1, DurationUnit.SECONDS)
            }
            install(Logging) {
                level = LogLevel.BODY
                logger = object: Logger {
                    override fun log(message: String) {
                        Clog.d(message)
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(HttpRequestRetry) {
                retryIf { _, response ->
                    !response.status.isSuccess() && response.status.value != 301 && response.status.value != 429 && response.status.value != 404
                }
                retryOnExceptionIf { _, cause ->
                    cause is ConnectTimeoutException || cause is JsonConvertException
                }
                exponentialDelay()
            }
        }
    }
}
