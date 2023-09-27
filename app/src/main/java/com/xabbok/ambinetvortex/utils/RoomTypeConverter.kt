package com.xabbok.ambinetvortex.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xabbok.ambinetvortex.dto.Attachment

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
