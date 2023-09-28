package com.xabbok.ambinetvortex.repository.di

import com.xabbok.ambinetvortex.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.xabbok.ambinetvortex.repository.PostRepositoryHTTPImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
    @Singleton
    @Binds
    fun bindsPostRepository(impl: PostRepositoryHTTPImpl) : PostRepository
}