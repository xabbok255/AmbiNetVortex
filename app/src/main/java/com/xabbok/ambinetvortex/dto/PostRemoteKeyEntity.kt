package com.xabbok.ambinetvortex.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val key: Long?
        ) {
    enum class KeyType {
        AFTER,
        BEFORE
    }
}