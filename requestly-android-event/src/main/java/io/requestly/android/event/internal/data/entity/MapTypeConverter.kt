package io.requestly.android.event.internal.data.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapTypeConverter {
    @TypeConverter
    fun stringToMap(value: String): Map<String, String> {
        return Gson().fromJson(value, object:TypeToken<Map<String, String>>() {}.type)
    }

    @TypeConverter
    fun mapToString(value: Map<String, String>): String {
        return if(value == null) "" else Gson().toJson(value)
    }
}
