package com.melonhead.data_user.models

@kotlinx.serialization.Serializable
data class UserResponse(
    val data: User
)

@kotlinx.serialization.Serializable
data class User(
    val id: String,
)
