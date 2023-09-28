package ru.netology.nmedia.repository

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.insertSeparators
import androidx.paging.map
import com.xabbok.ambinetvortex.api.ApiService
import com.xabbok.ambinetvortex.dao.PostDao
import com.xabbok.ambinetvortex.dao.PostRemoteKeyDao
import com.xabbok.ambinetvortex.db.AppDb
import com.xabbok.ambinetvortex.dto.Ad
import com.xabbok.ambinetvortex.dto.Attachment
import com.xabbok.ambinetvortex.dto.AttachmentType
import com.xabbok.ambinetvortex.dto.FeedItem
import com.xabbok.ambinetvortex.dto.Media
import com.xabbok.ambinetvortex.dto.NewerCount
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.dto.PostEntity
import com.xabbok.ambinetvortex.error.ApiAppError
import com.xabbok.ambinetvortex.presentation.MediaModel
import com.xabbok.ambinetvortex.repository.PostRemoteMediator
import com.xabbok.ambinetvortex.repository.PostRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject
import kotlin.random.Random

class PostRepositoryHTTPImpl @Inject constructor(
    private val dao: PostDao,
    val api: ApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
    @ApplicationContext
    context: Context
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            dao.getPagingSource()
        },
        remoteMediator = PostRemoteMediator(
            dao = dao,
            api = api,
            postRemoteKeyDao = postRemoteKeyDao,
            context = context,
            appDb = appDb
        )
    ).flow.map { pagingData ->
        pagingData
            .filter { postEntity ->
                postEntity.visible
            }
            .map {
                PostEntity.toDto(it)
            }
            .insertSeparators { prev, _ ->
                if (prev?.id?.rem(5) == 0L) {
                    Ad(Random.nextLong(), "figma.jpg")
                } else
                    null
            }
    }

    private val readMutex = Mutex()

    override suspend fun getNewerCount(): Flow<Long> = flow<Long> {
        while (true) {
            readMutex.withLock {
                runCatching {
                    val maxId = postRemoteKeyDao.max() ?: 0
                    val response = api.getNewerCount(maxId)

                    val body: NewerCount =
                        response.body() ?: throw ApiAppError(
                            response.code(),
                            response.message()
                        )
                    body.count
                }

            }.onSuccess {
                //val count = dao.getInvisibleCount()
                emit(it)
            }.onFailure {
                it.printStackTrace()
            }


            delay(10000)
        }
    }.flowOn(Dispatchers.Default)

    suspend fun getNewer(): Flow<Long> =
        flow<Long> {
            while (true) {
                emit(dao.getInvisibleCount())
                readMutex.withLock {
                    runCatching {
                        val biggestInvisibleId = dao.getBiggestInvisibleId()
                        val biggestVisibleId = dao.getBiggestVisibleId() ?: 0

                        val response = api.getNewer(biggestInvisibleId ?: biggestVisibleId)

                        val body: List<Post> =
                            response.body() ?: throw ApiAppError(
                                response.code(),
                                response.message()
                            )
                        val p = body
                            .map {
                                PostEntity.fromDto(it)
                                    .copy(visible = false)
                            }

                        dao.insertWithoutReplace(p)
                    }.onSuccess {
                        val count = dao.getInvisibleCount()
                        emit(count)
                    }.onFailure {
                        it.printStackTrace()
                    }
                }

                delay(10000)
            }
        }
            .flowOn(Dispatchers.Default)

    override suspend fun setAllVisible() {
        dao.setAllVisible()
    }


    override suspend fun getAll() {
        readMutex.withLock {
            val response = api.getAll()
            val posts = response.body().orEmpty()
            dao.clearAndInsert(posts.map { PostEntity.fromDto(it) })
        }
    }

    override suspend fun likeById(id: Long) {
        dao.likeById(id)
        runCatching {
            api.likeById(id)
        }.onFailure {
            dao.unlikeById(id)
            throw it
        }
    }

    override suspend fun unlikeById(id: Long) {
        dao.unlikeById(id)

        runCatching {
            api.unlikeById(id)
        }.onFailure {
            dao.likeById(id)
            throw it
        }
    }

    override suspend fun removeById(id: Long) {
        val post = dao.getById(id)
        post.let { originalPost ->
            dao.removeById(id)
            runCatching {
                api.removeById(id)
            }.onFailure {
                dao.save(post)
                throw it
            }
        }
    }

    override suspend fun addOrEditPost(post: Post) {
        api.addOrEditPost(post).body()?.also { newPost: Post ->
            dao.save(PostEntity.fromDto(newPost))
        }
    }

    override suspend fun addOrEditPostWithAttachment(post: Post, media: MediaModel) {
        val mediaUpload = upload(media)

        api.addOrEditPost(
            post.copy(
                attachment = Attachment(
                    url = mediaUpload.id,
                    type = AttachmentType.IMAGE
                )
            )
        ).body()?.also { newPost: Post ->
            dao.save(PostEntity.fromDto(newPost))
        }
    }

    private suspend fun upload(media: MediaModel): Media {
        requireNotNull(media.file).let { file ->
            val part =
                MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
            val response = requireNotNull(api.uploadMedia(part).body())

            return response
        }
    }
}

