package com.melonhead.mangadexfollower.models.user

@kotlinx.serialization.Serializable
data class UserResponse(
    val data: User
)

@kotlinx.serialization.Serializable
data class User(
    val id: String,
)