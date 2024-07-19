package com.melonhead.lib_networking.ratelimit.impl

import com.melonhead.lib_networking.ratelimit.RateLimit
import kotlin.time.DurationUnit

class RateLimitRuleBuilder(val config: RateLimit.Config) {
    var matcher: RequestMatcher = NoRequestMatcher()
    var keySelector: RequestKeySelector = NoRequestKeySelector()
}


interface RateLimitRuleBuilderWrapper {
    val builder: RateLimitRuleBuilder
}

class RequestMatcherWrapping(
    override val builder: RateLimitRuleBuilder
) : RateLimitRuleBuilderWrapper

/**
 * Matches requests by the given [matcher] function.
 */
fun RateLimit.Config.select(
    matcher: RequestMatcher
): RequestMatcherWrapping = RequestMatcherWrapping(
    RateLimitRuleBuilder(this).apply {
        this.matcher = matcher
    }
)

/**
 * Matches any given request (aka `else` branch in `when`).
 */
fun RateLimit.Config.default() =
    select { true }


/**
 * Sets the rate.
 */
fun RateLimitRuleBuilderWrapper.rate(block: MultiRateBuilder.() -> Unit) {
    val rules = MultiRateBuilder().apply(block).toRates()
    builder.config.rule(DefaultRateLimitRule(builder.matcher, builder.keySelector, rules))
}

/**
 * Sets the rate.
 */
fun RateLimitRuleBuilderWrapper.rate(
    permits: Int,
    period: Int,
    unit: DurationUnit,
) = rate { add(permits, period, unit) }
