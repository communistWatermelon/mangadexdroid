package com.melonhead.lib_core.extensions

import kotlinx.datetime.*
import java.time.format.DateTimeFormatter
import kotlin.math.abs

fun Instant.dateOrTimeString(useRelative: Boolean = false): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val sameDate = dateTime.dayOfYear == currentDate.dayOfYear
    val sameHour = sameDate && (dateTime.hour == currentDate.hour)
    val minuteDelta = abs(dateTime.minute - currentDate.minute)
    return when {
        useRelative && sameHour && minuteDelta <= 1 -> {
            "Just Now"
        }
        useRelative && sameHour && abs(dateTime.minute - currentDate.minute) <= 60 -> {
            "$minuteDelta minutes ago"
        }
        sameDate -> {
            val format = DateTimeFormatter.ofPattern("K:mm a")
            format.format(dateTime.toJavaLocalDateTime())
        }
        else -> {
            val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            format.format(dateTime.toJavaLocalDateTime())
        }
    }
}
