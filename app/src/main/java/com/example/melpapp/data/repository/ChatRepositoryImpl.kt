package com.example.melpapp.data.repository

import com.example.melpapp.data.local.ChatDao
import com.example.melpapp.data.mapper.toDomain
import com.example.melpapp.data.mapper.toEntity
import com.example.melpapp.data.remote.ApiService
import com.example.melpapp.domain.model.RecentChat
import com.example.melpapp.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class ChatRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val chatDao: ChatDao
) : ChatRepository {

    override fun observeChats(): Flow<List<RecentChat>> {
        return chatDao.observeChats()
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun refreshChats(): Long {
        return withContext(Dispatchers.IO) {

            var duration = 0L
            val users = mutableListOf<com.example.melpapp.data.remote.UserDto>()

            duration = measureTimeMillis {
                val response = apiService.getUsers()
                users.addAll(response.users)
            }

            // 👉 FINAL: No 40 copies, no counter, no fake ID generation.
            val entities = users.map { user ->
                user.toEntity()   // uses the REAL user.id
            }

            chatDao.upsertChats(entities)

            duration
        }
    }
}
