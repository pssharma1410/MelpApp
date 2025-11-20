package com.example.melpapp.domain.repository

import com.example.melpapp.domain.model.RecentChat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChats(): Flow<List<RecentChat>>
    suspend fun refreshChats(): Long // API response time ms
}
