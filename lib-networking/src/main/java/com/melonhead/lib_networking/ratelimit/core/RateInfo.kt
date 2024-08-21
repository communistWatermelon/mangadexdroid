package com.melonhead.lib_networking.ratelimit.core

import kotlin.time.DurationUnit

internal data class RateInfo(
    /**
     * Number of permits for a given [period].
     */
    val permits: Int,
    /**
     * The period in [unit] units.
     */
    val period: Int,
    /**
     * Period units.
     *
     * @see period
     */
    val unit: DurationUnit,
)
