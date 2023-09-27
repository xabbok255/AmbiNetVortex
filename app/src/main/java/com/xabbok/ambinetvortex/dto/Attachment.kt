package com.xabbok.ambinetvortex.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Attachment(
    val url: String,
    //val description: String?,
    val type: AttachmentType,
) : Parcelable

enum class AttachmentType {
    IMAGE
}