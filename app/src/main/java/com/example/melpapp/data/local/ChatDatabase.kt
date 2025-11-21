package com.example.melpapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 9,
    exportSchema = false
)

abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}

