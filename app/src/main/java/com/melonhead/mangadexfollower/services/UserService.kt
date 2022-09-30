package com.melonhead.mangadexfollower.services

import android.util.Log
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.content.Chapter
import com.melonhead.mangadexfollower.models.shared.PaginatedResponse
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

interface UserService {
    suspend fun getFollowedChapters(token: AuthToken): PaginatedResponse<List<Chapter>>
}

class UserServiceImpl(
    private val client: HttpClient
): UserService {
    override suspend fun getFollowedChapters(token: AuthToken): PaginatedResponse<List<Chapter>> {
        val result = client.get(HttpRoutes.USER_FOLLOW_CHAPTERS_URL) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
            url {
                encodedParameters.append("translatedLanguage[]", "en")
                parameters.append("order[createdAt]", "desc")
                parameters.append("limit", "100")
            }
        }
        return try {
            result.body()
        } catch (e: Exception) {
            Log.w("", "getFollowedChapters: ${result.bodyAsText()}")
            e.printStackTrace()
            PaginatedResponse(0, 0, 0, listOf())
        }
    }
}