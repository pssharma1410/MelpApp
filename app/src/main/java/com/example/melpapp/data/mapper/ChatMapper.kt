package com.example.melpapp.data.mapper

import com.example.melpapp.data.local.ChatEntity
import com.example.melpapp.data.remote.UserDto
import com.example.melpapp.domain.model.RecentChat
import kotlin.random.Random

fun UserDto.toEntity(): ChatEntity {
    return ChatEntity(
        id = this.id,
        name = "$firstName $lastName",
        lastMessage = listOf(
            "Hey, what's up?",
            "I’ll get back to you.",
            "Let’s meet tomorrow.",
            "Okay!",
            "Sounds good."
        ).random(),
        unreadCount = Random.nextInt(0, 8),
        lastSeen = generateRealisticTimestamp(),
        isOnline = Random.nextBoolean(),
        avatarUrl = image ?: ""
    )
}
fun generateRealisticTimestamp(): Long {
    val now = System.currentTimeMillis()

    return when (Random.nextInt(5)) {

        // 0 → Within last few minutes
        0 -> now - Random.nextLong(
            1 * 60 * 1000L,
            60 * 60 * 1000L
        ) // 1 min to 60 min ago

        // 1 → Within last few hours
        1 -> now - Random.nextLong(
            1 * 60 * 60 * 1000L,
            12 * 60 * 60 * 1000L
        ) // 1–12 hours ago

        // 2 → Yesterday
        2 -> now - Random.nextLong(
            24 * 60 * 60 * 1000L,
            48 * 60 * 60 * 1000L
        )

        // 3 → This week (2–6 days ago)
        3 -> now - Random.nextLong(
            2L * 24L * 60L * 60L * 1000L,
            7L * 24L * 60L * 60L * 1000L
        )

        // 4 → Last week (7–14 days ago)
        else -> now - Random.nextLong(
            7L * 24L * 60L * 60L * 1000L,
            14L * 24L * 60L * 60L * 1000L
        )
    }
}



fun ChatEntity.toDomain(): RecentChat {
    return RecentChat(
        id = id,
        name = name,
        lastMessage = lastMessage,
        unreadCount = unreadCount,
        lastSeen = lastSeen,
        isOnline = isOnline,
        avatarUrl = avatarUrl
    )
}
