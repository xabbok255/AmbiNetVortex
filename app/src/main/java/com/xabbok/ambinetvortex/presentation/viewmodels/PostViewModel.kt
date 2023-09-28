package com.xabbok.ambinetvortex.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.dto.FeedItem
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    appAuth: AppAuth
    //private val scope: CoroutineScope = MainScope()
) : ViewModel() {
    private val cached = repository
        .data
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth.data.flatMapLatest { auth ->
        cached
            .map { posts ->
                posts.map {
                    if (it is Post) {
                        it.copy(ownedByMe = auth?.id == it.authorId)
                    } else
                        it
                }
            }

    }.flowOn(Dispatchers.Default)

}