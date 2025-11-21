package com.example.melpapp.di

import com.example.melpapp.data.repository.ChatRepositoryImpl
import com.example.melpapp.data.repository.MessageRepositoryImpl
import com.example.melpapp.domain.repository.ChatRepository
import com.example.melpapp.domain.repository.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepo(
        impl: MessageRepositoryImpl
    ):MessageRepository

}
