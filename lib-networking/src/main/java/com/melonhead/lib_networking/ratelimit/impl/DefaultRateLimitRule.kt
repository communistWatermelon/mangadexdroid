package com.melonhead.lib_networking.ratelimit.impl

import com.melonhead.lib_networking.ratelimit.core.RateInfo
import com.melonhead.lib_networking.ratelimit.core.RateLimitRule
import io.ktor.client.request.*
import io.ktor.util.collections.*

class DefaultRateLimitRule(
    val matcher: RequestMatcher,
    val keySelector: RequestKeySelector,
    override val rates: List<RateInfo>,
) : RateLimitRule {
    private val groupHandlers = ConcurrentMap<RequestKey, RateLimitHandler>()

    override fun isMatch(request: HttpRequestBuilder): Boolean {
        return matcher(request)
    }

    override suspend fun onSendRequest(request: HttpRequestBuilder) {
        groupHandlers.computeIfAbsent(keySelector(request)) {
            RateLimitHandler(rates)
        }.handle()
    }
}
