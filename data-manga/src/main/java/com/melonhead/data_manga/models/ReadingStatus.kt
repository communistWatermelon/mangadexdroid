package com.melonhead.data_manga.models

import kotlinx.serialization.Serializable

@Serializable
sealed class ReadingStatus {
    data object Reading: ReadingStatus()
    data object OnHold: ReadingStatus()
    data object PlanToRead: ReadingStatus()
    data object Dropped: ReadingStatus()
    data object ReReading: ReadingStatus()
    data object Completed: ReadingStatus()

    fun serialized(): String {
        return when (this) {
            Completed -> "completed"
            Dropped -> "dropped"
            OnHold -> "on_hold"
            PlanToRead -> "plan_to_read"
            ReReading -> "re_reading"
            Reading -> "reading"
        }
    }

    companion object {
        fun from(string: String?): ReadingStatus? {
            return when (string) {
                "completed" -> Completed
                "dropped" -> Dropped
                "on_hold" -> OnHold
                "plan_to_read" -> PlanToRead
                "re_reading" -> ReReading
                "reading" -> Reading
                else -> null
            }
        }
    }
}
