package com.xabbok.ambinetvortex.di

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class FirebaseMsgModule {

    @Provides
    fun provideFirebase() : FirebaseMessaging = FirebaseMessaging.getInstance()
}