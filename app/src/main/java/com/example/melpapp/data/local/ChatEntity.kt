package com.example.melpapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val lastMessage: String,
    val unreadCount: Int,
    val lastSeen: Long,
    val isOnline: Boolean,
    val avatarUrl: String
)

