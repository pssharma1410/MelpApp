package com.example.melpapp.data.repository

import com.example.melpapp.data.local.MessageDao
import com.example.melpapp.data.local.MessageEntity
import com.example.melpapp.domain.model.Message
import com.example.melpapp.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val dao: MessageDao
) : MessageRepository {

    override fun getMessages(chatId: Int): Flow<List<Message>> =
        dao.getMessages(chatId)
            .onEach { list ->
                // If no messages exist for this chat → generate some mock messages
                if (list.isEmpty()) {
                    val fakeMessages = listOf(
                        MessageEntity(
                            chatId = chatId,
                            text = "Hey! How are you?",
                            isMe = false,
                            timestamp = System.currentTimeMillis() - 60000
                        ),
                        MessageEntity(
                            chatId = chatId,
                            text = "I'm good, just working on something.",
                            isMe = true,
                            timestamp = System.currentTimeMillis() - 30000
                        ),
                        MessageEntity(
                            chatId = chatId,
                            text = "Nice! Let's catch up later.",
                            isMe = false,
                            timestamp = System.currentTimeMillis() - 10000
                        )
                    )

                    dao.insertMessages(fakeMessages)
                }
            }
            .map { list ->
                list.map {
                    Message(
                        id = it.id,
                        chatId = it.chatId,
                        text = it.text,
                        isMe = it.isMe,
                        timestamp = it.timestamp
                    )
                }
            }
}
