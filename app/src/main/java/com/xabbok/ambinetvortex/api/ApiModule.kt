package com.xabbok.ambinetvortex.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.xabbok.ambinetvortex.BuildConfig
import com.xabbok.ambinetvortex.auth.AppAuth
import com.xabbok.ambinetvortex.error.ApiAppError
import com.xabbok.ambinetvortex.error.NetworkAppError
import com.xabbok.ambinetvortex.error.UnknownAppError
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .let {
            if (BuildConfig.DEBUG) {
                it.addInterceptor(logging)
            } else
                it
        }
        .addInterceptor(Interceptor { chain ->
            val request: Request = chain.request()

            val response: okhttp3.Response = try {
                chain.proceed(request)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                throw NetworkAppError
            } catch (e: Exception) {
                throw UnknownAppError
            }

            if (!response.isSuccessful) {
                response.close()
                throw ApiAppError(response.code, response.message)
            }

            response
        })
        .addInterceptor(Interceptor { chain ->
            val request = appAuth.data.value?.token?.let {
                chain.request().newBuilder()
                    .addHeader("Authorization", it)
                    .build()
            } ?: chain.request()

            chain.proceed(request)
        })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL_API)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService = retrofit.create(ApiService::class.java)
}