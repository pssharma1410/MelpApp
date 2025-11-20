package com.example.melpapp.data.remote

import retrofit2.http.GET

data class UsersResponse(
    val users: List<UserDto>
)

data class UserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val image: String?
)

interface ApiService {

    @GET("users")
    suspend fun getUsers(): UsersResponse
}
