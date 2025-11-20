package com.example.melpapp.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.melpapp.domain.model.RecentChat
import com.example.melpapp.domain.usecase.GetRecentChatsUseCase
import com.example.melpapp.domain.usecase.RefreshChatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getRecentChatsUseCase: GetRecentChatsUseCase,
    private val refreshChatsUseCase: RefreshChatsUseCase
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 30
        private const val TAG = "ChatListVM"
    }

    private val _state = MutableStateFlow(ChatListUiState())
    val state = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ChatListUiState()
    )

    private val searchQuery = MutableStateFlow("")
    private val _typingUsers = MutableStateFlow<Set<Int>>(emptySet())
    val typingUsers = _typingUsers.asStateFlow()

    private var currentPage = 1
    private var typingJob: Job? = null

    init {
        Timber.tag(TAG).d("INIT: ViewModel started")
        observeChats()
        observeSearch()
        simulateTyping()
        refresh()
        _state.value = _state.value.copy(searchText = "")
    }

    private fun logList(tag: String, list: List<RecentChat>) {
        Timber.tag(TAG).d("$tag => size=${list.size}")
        val duplicates = list.groupBy { it.id }.filter { it.value.size > 1 }
        duplicates.forEach {
            Timber.tag(TAG).e("$tag DUPLICATE -> ID=${it.key}, count=${it.value.size}")
        }
    }

    private fun observeChats() {
        viewModelScope.launch {
            getRecentChatsUseCase().collectLatest { chats ->

                Timber.tag(TAG).d("observeChats() RAW size=${chats.size}")
                logList("RAW_FROM_DB", chats)

                val distinct = chats.distinctBy { it.id }
                val sortedChats = sortChats(distinct)

                logList("AFTER_SORT", sortedChats)

                val displayed = applyPaginationAndFilter(sortedChats, searchQuery.value)
                logList("DISPLAYED_AFTER_OBSERVE", displayed)

                _state.value = _state.value.copy(
                    allChats = sortedChats,
                    displayedChats = displayed
                )
            }
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .collectLatest { query ->

                    Timber.tag(TAG).d("SEARCH_TRIGGERED: '$query'")

                    val chats = _state.value.allChats.distinctBy { it.id }
                    val displayed = applyPaginationAndFilter(chats, query)

                    logList("DISPLAYED_AFTER_SEARCH", displayed)

                    _state.value = _state.value.copy(displayedChats = displayed)
                }
        }
    }

    private fun sortChats(chats: List<RecentChat>): List<RecentChat> {
        Timber.tag(TAG).d("SORTING size=${chats.size}")

        val sorted = chats
            .distinctBy { it.id }
            .sortedWith(
                compareByDescending<RecentChat> { it.lastSeen }
                    .thenByDescending { it.id }
            )

        logList("SORT_RESULT", sorted)

        return sorted
    }

    private fun applyPaginationAndFilter(
        chats: List<RecentChat>,
        query: String
    ): List<RecentChat> {

        Timber.tag(TAG).d("applyPaginationAndFilter incoming=${chats.size}, query='$query'")
        logList("APPLY_FILTER_INCOMING", chats)

        val distinctChats = chats.distinctBy { it.id }
        logList("AFTER_DISTINCT_FILTER", distinctChats)

        val filtered = if (query.isBlank()) {
            distinctChats
        } else {
            distinctChats.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.lastMessage.contains(query, ignoreCase = true)
            }
        }

        val limit = (currentPage * PAGE_SIZE).coerceAtMost(filtered.size)
        val final = filtered.take(limit)

        logList("AFTER_PAGINATION", final)

        return final
    }

    fun onSearchChange(value: String) {
        Timber.tag(TAG).d("SEARCH_CHANGE='$value'")
        searchQuery.value = value
        currentPage = 1
        _state.value = _state.value.copy(searchText = value)
    }

    fun loadMore() {
        Timber.tag(TAG).d("LOAD_MORE triggered")

        val allFiltered = applyPaginationAndFilter(
            _state.value.allChats.distinctBy { it.id },
            searchQuery.value
        )

        if (_state.value.displayedChats.size < allFiltered.size) {
            currentPage++

            val newList = applyPaginationAndFilter(
                _state.value.allChats.distinctBy { it.id },
                searchQuery.value
            )

            logList("DISPLAYED_AFTER_LOAD_MORE", newList)

            _state.value = _state.value.copy(displayedChats = newList)
        }
    }

    fun refresh() {
        viewModelScope.launch {

            Timber.tag(TAG).d("REFRESH called")

            _state.value = _state.value.copy(
                isRefreshing = true,
                errorMessage = null
            )

            try {
                val timeMs = refreshChatsUseCase()

                val newChats = getRecentChatsUseCase().take(1).single()

                Timber.tag(TAG).d("REFRESH RAW size=${newChats.size}")
                logList("REFRESH_RAW", newChats)

                val distinct = newChats.distinctBy { it.id }
                val sortedChats = sortChats(distinct)

                val displayed = applyPaginationAndFilter(sortedChats, searchQuery.value)
                logList("DISPLAYED_AFTER_REFRESH", displayed)

                _state.value = _state.value.copy(
                    allChats = sortedChats,
                    displayedChats = displayed,
                    isRefreshing = false,
                    apiResponseTimeMs = timeMs
                )

            } catch (e: Exception) {
                Timber.tag(TAG).e("REFRESH ERROR: ${e.message}")
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    errorMessage = e.message ?: "Something went wrong"
                )
            }
        }
    }

    private fun simulateTyping() {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            while (true) {
                delay(4000)

                val list = _state.value.displayedChats.distinctBy { it.id }
                if (list.isNotEmpty()) {

                    val randomChat = list.random()

                    Timber.tag(TAG).d("TYPING START id=${randomChat.id}")

                    val current = _typingUsers.value.toMutableSet()
                    current.add(randomChat.id)
                    _typingUsers.value = current

                    val duration = Random.nextLong(3000L, 5000L)

                    launch {
                        delay(duration)

                        Timber.tag(TAG).d("TYPING END id=${randomChat.id}")

                        val updated = _typingUsers.value.toMutableSet()
                        updated.remove(randomChat.id)
                        _typingUsers.value = updated
                    }
                }
            }
        }
    }

    fun markChatAsRead(chatId: Int) {
        Timber.tag(TAG).d("MARK_CHAT_AS_READ id=$chatId")

        val updatedList = _state.value.allChats
            .distinctBy { it.id }
            .map {
                if (it.id == chatId) it.copy(unreadCount = 0) else it
            }

        logList("MARK_READ_UPDATED_LIST", updatedList)

        val sortedUpdatedList = sortChats(updatedList)
        val displayed = applyPaginationAndFilter(sortedUpdatedList, searchQuery.value)

        logList("DISPLAYED_AFTER_MARK_READ", displayed)

        _state.value = _state.value.copy(
            allChats = sortedUpdatedList,
            displayedChats = displayed
        )
    }
}
