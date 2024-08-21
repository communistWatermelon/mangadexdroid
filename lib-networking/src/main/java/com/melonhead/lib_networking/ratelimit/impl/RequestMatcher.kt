package com.melonhead.lib_networking.ratelimit.impl
import io.ktor.client.request.*

internal fun interface RequestMatcher {
    operator fun invoke(request: HttpRequestBuilder): Boolean
}


internal class NoRequestMatcher : RequestMatcher {
    override fun invoke(request: HttpRequestBuilder): Boolean {
        return true
    }
}

internal class RequestMatcherNot(
    val operand: RequestMatcher
) : RequestMatcher {
    override fun invoke(request: HttpRequestBuilder): Boolean {
        return operand(request).not()
    }
}

internal fun RequestMatcher.not() = RequestMatcherNot(this)
