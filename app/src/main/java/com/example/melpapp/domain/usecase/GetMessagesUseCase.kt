package com.example.melpapp.domain.usecase

import com.example.melpapp.domain.repository.MessageRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repo: MessageRepository
) {
    operator fun invoke(chatId: Int) = repo.getMessages(chatId)
}
