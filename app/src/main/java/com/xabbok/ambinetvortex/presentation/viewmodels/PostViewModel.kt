package com.xabbok.ambinetvortex.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.dto.FeedItem
import com.xabbok.ambinetvortex.dto.NON_EXISTING_POST_ID
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.error.ScreenState
import com.xabbok.ambinetvortex.presentation.MediaModel
import com.xabbok.ambinetvortex.repository.PostRepository
import com.xabbok.ambinetvortex.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
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

    private val _state = MutableLiveData<ScreenState>(ScreenState.Working())

    val state: LiveData<ScreenState>
        get() = _state

    private val _fragmentEditPostEdited = SingleLiveEvent<Unit>()

    val fragmentEditPostEdited: LiveData<Unit>
        get() = _fragmentEditPostEdited

    val edited: MutableLiveData<Post?> = MutableLiveData(null)

    private val emptyMedia = MediaModel()

    private val _media = MutableLiveData<MediaModel>(emptyMedia)
    val media: LiveData<MediaModel>
        get() = _media

    fun changeState(newState: ScreenState) {
        _state.postValue(newState)
    }

    var draft: Post? = null

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val newerCount: Flow<Long> = data.flatMapLatest {
        repository.getNewerCount()
            .catch { e ->
                e.printStackTrace()
            }
    }.map {
        it.toLong()
    }

    fun clearPhoto() {
        _media.value = emptyMedia
    }

    fun changePhoto(file: File, uri: Uri) {
        _media.value = MediaModel(file = file, uri = uri)
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            runCatching {
                //changeState(ScreenState.Loading())

                if (!post.likedByMe) {
                    repository.likeById(post.id)
                } else {
                    repository.unlikeById(post.id)
                }
            }

                //changeState(ScreenState.Working())
                .onFailure { e ->
                    changeState(
                        ScreenState.Error(
                            message = e.message.toString(),
                            repeatText = "Ошибка при обработке лайка!"
                        ) {
                            likeById(post)
                        }
                    )
                }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            runCatching {
                //changeState(ScreenState.Loading())
                repository.removeById(id)
                //changeState(ScreenState.Working())
            }.onFailure { e ->
                changeState(
                    ScreenState.Error(
                        message = e.message.toString(),
                        repeatText = "Ошибка при удалении!"
                    ) {
                        removeById(id)
                    }
                )
            }
        }
    }

    fun addOrEditPost(post: Post) {
        var addingPost = post
        edited.value?.let {
            addingPost = it.copy(content = post.content)
        }

        viewModelScope.launch {
            try {
                media.value?.let { media ->
                    when (media) {
                        emptyMedia -> repository.addOrEditPost(addingPost)
                        else -> repository.addOrEditPostWithAttachment(addingPost, media)
                    }

                    //changeState(ScreenState.Loading())
                    edited.postValue(null)
                    clearPhoto()

                    if (post.id == NON_EXISTING_POST_ID)
                        repository.setAllVisible()

                    changeState(ScreenState.Working(moveRecyclerViewPointerToTop = (post.id == NON_EXISTING_POST_ID)))
                    _fragmentEditPostEdited.postValue(Unit)
                }
            } catch (e: Exception) {
                _fragmentEditPostEdited.postValue(Unit)
                changeState(ScreenState.Error(e.message.toString()))
            }
        }
    }

}