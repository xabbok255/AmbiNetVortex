package com.xabbok.ambinetvortex.repository

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.xabbok.ambinetvortex.api.ApiService
import com.xabbok.ambinetvortex.dao.PostDao
import com.xabbok.ambinetvortex.dao.PostRemoteKeyDao
import com.xabbok.ambinetvortex.db.AppDb
import com.xabbok.ambinetvortex.dto.PostEntity
import com.xabbok.ambinetvortex.dto.PostRemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val dao: PostDao,
    val api: ApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    val context: Context,
    private val appDb: AppDb
) :
    RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        return try {
            val response = when (loadType) {
                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min()
                        ?: return MediatorResult.Success(false)
                    api.getBefore(id, state.config.pageSize)
                }

                LoadType.REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    id?.let {
                        api.getNewer(id)
                    } ?: api.getLatest(state.config.initialLoadSize)

                }
            }

            val posts = response.body().orEmpty().map {
                PostEntity.fromDto(it)
            }

            //Toast.makeText(context, loadType.name, Toast.LENGTH_LONG).show()

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        dao.clear()
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                null
                            )
                        )

                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                posts.lastOrNull()?.id
                            )
                        )

                    }

                    LoadType.PREPEND -> {
                        if (posts.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    posts.first().id
                                )
                            )
                        }
                    }

                    LoadType.APPEND -> {
                        if (posts.isNotEmpty()) {
                            postRemoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    posts.last().id
                                )
                            )
                        }
                    }
                }

                if (posts.isNotEmpty())
                    dao.save(posts)
            }
            return MediatorResult.Success(posts.isEmpty())
        } catch (e: Exception) {
            //Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            MediatorResult.Error(e)
        }
    }
}