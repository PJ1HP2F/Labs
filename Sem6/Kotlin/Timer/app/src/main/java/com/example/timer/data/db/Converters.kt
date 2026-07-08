package com.example.timerapp.data.db

import androidx.room.TypeConverter
import com.example.timerapp.data.model.Phase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPhaseList(value: List<Phase>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPhaseList(value: String): List<Phase> {
        val type = object : TypeToken<List<Phase>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
