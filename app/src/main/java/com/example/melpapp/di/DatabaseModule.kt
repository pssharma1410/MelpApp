package com.example.melpapp.di

import android.content.Context
import androidx.room.Room
import com.example.melpapp.data.local.ChatDao
import com.example.melpapp.data.local.ChatDatabase
import com.example.melpapp.data.local.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChatDatabase =
        Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "chat_db"

        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideChatDao(db: ChatDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideMessageDao(db: ChatDatabase): MessageDao = db.messageDao()


}
