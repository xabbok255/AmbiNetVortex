package com.xabbok.ambinetvortex.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xabbok.ambinetvortex.dao.PostDao
import com.xabbok.ambinetvortex.dao.PostRemoteKeyDao
import com.xabbok.ambinetvortex.dto.PostEntity
import com.xabbok.ambinetvortex.dto.PostRemoteKeyEntity

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 5, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}
