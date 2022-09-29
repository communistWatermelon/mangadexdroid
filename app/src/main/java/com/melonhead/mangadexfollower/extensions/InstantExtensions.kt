package com.melonhead.mangadexfollower.extensions

import kotlinx.datetime.*
import java.time.format.DateTimeFormatter

fun Instant.dateOrTimeString(): String {
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val format = if (dateTime.dayOfYear == currentDate.dayOfYear) {
        DateTimeFormatter.ofPattern("K:mm a")
    } else {
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
    return format.format(dateTime.toJavaLocalDateTime())
}
