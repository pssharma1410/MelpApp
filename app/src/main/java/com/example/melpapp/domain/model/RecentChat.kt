package com.example.melpapp.domain.model

data class RecentChat(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val unreadCount: Int,
    val lastSeen: Long,
    val isOnline: Boolean,
    val avatarUrl: String
)