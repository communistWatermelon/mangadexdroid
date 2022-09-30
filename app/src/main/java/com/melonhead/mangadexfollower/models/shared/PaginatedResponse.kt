package com.melonhead.mangadexfollower.models.shared

@kotlinx.serialization.Serializable
data class PaginatedResponse<T>(val limit: Int, val offset: Int, val total: Int, val data: T)