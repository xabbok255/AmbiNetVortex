package com.xabbok.ambinetvortex.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xabbok.ambinetvortex.dto.Attachment
import java.util.Date

class AttachmentConverter {
    @TypeConverter
    fun from(data: Attachment?): String {
        val value = Gson().toJson(data, object : TypeToken<Attachment?>() {}.type)
        return value
    }

    @TypeConverter
    fun to(s: String): Attachment? {
        val value = Gson().fromJson<Attachment?>(s, object : TypeToken<Attachment?>() {}.type)
        return value
    }
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
