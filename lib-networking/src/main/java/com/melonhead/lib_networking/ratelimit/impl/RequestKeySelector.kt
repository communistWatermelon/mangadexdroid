package com.melonhead.lib_networking.ratelimit.impl
import io.ktor.client.request.*

internal fun interface RequestKeySelector {
    operator fun invoke(request: HttpRequestBuilder): RequestKey
}

internal class NoRequestKeySelector : RequestKeySelector {
    private val requestKey = Any()

    override fun invoke(request: HttpRequestBuilder): RequestKey {
        return requestKey
    }
}

internal typealias RequestKey = Any
