package com.melonhead.lib_networking.ratelimit.impl
import io.ktor.client.request.*

fun interface RequestMatcher {
    operator fun invoke(request: HttpRequestBuilder): Boolean
}


class NoRequestMatcher : RequestMatcher {
    override fun invoke(request: HttpRequestBuilder): Boolean {
        return true
    }
}

class RequestMatcherNot(
    val operand: RequestMatcher
) : RequestMatcher {
    override fun invoke(request: HttpRequestBuilder): Boolean {
        return operand(request).not()
    }
}

fun RequestMatcher.not() = RequestMatcherNot(this)
