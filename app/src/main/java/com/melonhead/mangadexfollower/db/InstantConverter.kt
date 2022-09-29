package com.melonhead.mangadexfollower.db

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

object InstantConverter {
    @TypeConverter
    fun toInstant(dateMillis: Long?): Instant? {
        return dateMillis?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.epochSeconds
    }
}