package com.melonhead.mangadexfollower.ratelimit.impl

import com.melonhead.mangadexfollower.ratelimit.core.RateInfo
import kotlin.time.DurationUnit

class MultiRateBuilder {
    private val rates = ArrayList<RateInfo>()

    private fun add(rate: RateInfo) {
        rates.add(rate)
    }

    fun add(
        permits: Int,
        period: Int,
        unit: DurationUnit,
    ) = add(RateInfo(permits, period, unit))

    internal fun toRates() = rates.toList()
}