package com.example.melpapp.ui.chatlist

import com.example.melpapp.domain.model.RecentChat

data class ChatListUiState(
    val allChats: List<RecentChat> = emptyList(),
    val displayedChats: List<RecentChat> = emptyList(),
    val isRefreshing: Boolean = false,
    val apiResponseTimeMs: Long? = null,
    val errorMessage: String? = null,
    val searchText: String = ""
)
