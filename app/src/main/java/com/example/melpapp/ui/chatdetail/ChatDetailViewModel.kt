package com.example.melpapp.ui.chatdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melpapp.domain.model.Message
import com.example.melpapp.domain.usecase.GetMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getMessagesUseCase: GetMessagesUseCase,
) : ViewModel() {

    private val chatId: Int = savedStateHandle["chatId"] ?: 0


    private val _localMessages = MutableStateFlow<List<Message>>(emptyList())


    val messages: StateFlow<List<Message>> =
        combine(
            getMessagesUseCase(chatId),
            _localMessages
        ) { remoteMessages, localMessages ->
            remoteMessages + localMessages
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )


    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val newMessage = Message(
            text = text,
            isMe = true
        )

        _localMessages.value = _localMessages.value + newMessage
    }
}
