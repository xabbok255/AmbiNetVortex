package com.xabbok.ambinetvortex.repository

import androidx.paging.PagingData
import com.xabbok.ambinetvortex.dto.FeedItem
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.presentation.MediaModel
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun getAll()
    suspend fun likeById(id: Long)
    suspend fun unlikeById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun addOrEditPost(post: Post)

    suspend fun addOrEditPostWithAttachment(post: Post, media: MediaModel)

    val data: Flow<PagingData<FeedItem>>

    suspend fun getNewerCount() : Flow<Long>

    suspend fun setAllVisible()
}