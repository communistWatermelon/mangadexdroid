package com.melonhead.mangadexfollower.services

import android.util.Log
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.auth.PaginatedResponse
import com.melonhead.mangadexfollower.models.content.Chapter
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

interface UserService {
    suspend fun getFollowedChapters(token: AuthToken, createdAtSince: Long): PaginatedResponse<List<Chapter>>
}

class UserServiceImpl(
    private val client: HttpClient
): UserService {
    override suspend fun getFollowedChapters(token: AuthToken, createdAtSince: Long): PaginatedResponse<List<Chapter>> {
        val result = client.get(HttpRoutes.USER_FOLLOW_CHAPTERS_URL) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
            url {
                encodedParameters.append("translatedLanguage[]", "en")
                parameters.append("order[createdAt]", "desc")
                parameters.append("limit", "500")
                if (createdAtSince != 0L) {
                    val date = Date()
                    date.time = createdAtSince
                    val utcDate = date.toInstant().atZone(ZoneId.of("UTC"))
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'kk:mm:ss")
                    encodedParameters.append("createdAtSince", formatter.format(utcDate))
                }
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