package com.melonhead.lib_database

import androidx.room.TypeConverter

internal class ListConverter {
    @TypeConverter
    fun listToJson(value: List<String>): String {
        return value.joinToString(separator = "*,*") { it }
    }

    @TypeConverter
    fun jsonToList(value: String): List<String> {
        return value.split("*,*")
    }
}
