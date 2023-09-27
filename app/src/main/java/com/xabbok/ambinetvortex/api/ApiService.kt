package com.xabbok.ambinetvortex.api

import okhttp3.*
import retrofit2.Response
import retrofit2.http.*
import com.xabbok.ambinetvortex.dto.Media
import com.xabbok.ambinetvortex.dto.NewerCount
import com.xabbok.ambinetvortex.dto.Post
import com.xabbok.ambinetvortex.dto.PushToken
import com.xabbok.ambinetvortex.error.*
import com.xabbok.ambinetvortex.presentation.AuthModel

interface ApiService {
    @POST("users/push-tokens")
    suspend fun savePushToken(@Body pushToken: PushToken): Response<Unit>

    @Multipart
    @POST("users/registration")
    suspend fun registerUser(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part? = null,
    ): Response<AuthModel>

    /*@FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<AuthModel>*/

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun auth(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<AuthModel>

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count : Int): Response<List<Post>>

    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part part: MultipartBody.Part): Response<Media>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts")
    suspend fun addOrEditPost(@Body post: Post): Response<Post>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Long): Response<Post>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{id}/newer-count")
    suspend fun getNewerCount(@Path("id") id: Long): Response<NewerCount>

    @GET("posts/{id}/before")
    suspend fun getBefore(@Path("id") id: Long, @Query("count") count : Int): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfter(@Path("id") id: Long, @Query("count") count : Int): Response<List<Post>>
}





