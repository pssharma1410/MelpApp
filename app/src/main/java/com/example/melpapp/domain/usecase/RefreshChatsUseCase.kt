package com.example.melpapp.domain.usecase

import com.example.melpapp.domain.repository.ChatRepository
import javax.inject.Inject

class RefreshChatsUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    // Passes up the exception thrown by repository.refreshChats()
    suspend operator fun invoke(): Long = repository.refreshChats()
}
