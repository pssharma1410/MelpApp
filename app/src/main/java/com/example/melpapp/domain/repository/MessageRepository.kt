package com.example.melpapp.domain.repository

import com.example.melpapp.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessages(chatId: Int): Flow<List<Message>>
}
