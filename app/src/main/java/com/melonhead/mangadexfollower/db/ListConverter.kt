package com.melonhead.mangadexfollower.db

import androidx.room.TypeConverter

class ListConverter {
    @TypeConverter
    fun listToJson(value: List<String>): String {
        return value.joinToString(separator = "*,*") { it }
    }

    @TypeConverter
    fun jsonToList(value: String): List<String> {
        return value.split("*,*")
    }
}