package com.example.melpapp.domain.usecase

import com.example.melpapp.domain.model.RecentChat
import com.example.melpapp.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentChatsUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<List<RecentChat>> = repository.observeChats()
}
