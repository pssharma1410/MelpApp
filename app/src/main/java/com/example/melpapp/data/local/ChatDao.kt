package com.example.melpapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM recent_chats ORDER BY lastSeen DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    // FIX: Upsert avoids empty-emission problem
    @Upsert
    suspend fun upsertChats(chats: List<ChatEntity>)
}
