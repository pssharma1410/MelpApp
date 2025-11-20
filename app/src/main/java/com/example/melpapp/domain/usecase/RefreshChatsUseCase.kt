package com.example.melpapp.domain.usecase

import com.example.melpapp.domain.repository.ChatRepository
import javax.inject.Inject

class RefreshChatsUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Long = repository.refreshChats()
}
