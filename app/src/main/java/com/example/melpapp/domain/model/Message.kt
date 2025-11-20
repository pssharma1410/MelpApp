package com.example.melpapp.domain.model

data class Message(
    val id: Int = 0,
    val chatId: Int = 0,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isMe: Boolean = false
)
