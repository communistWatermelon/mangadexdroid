package com.melonhead.mangadexfollower.services

import android.util.Log
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.cover.Cover
import com.melonhead.mangadexfollower.models.cover.CoverResponse
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

interface CoverService {
    suspend fun getCovers(token: AuthToken, mangaIds: List<String>): List<Cover>
}

class CoverServiceImpl(
    private val client: HttpClient
): CoverService {
    override suspend fun getCovers(token: AuthToken, mangaIds: List<String>): List<Cover> {
        val result = client.get(HttpRoutes.COVER_URL) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
            url {
                mangaIds.forEach { encodedParameters.append("manga[]", it) }
                parameters.append("limit", "100")
            }
        }
        return try {
            result.body<CoverResponse>().data
        } catch (e: Exception) {
            Log.w("", "getFollowedChapters: ${result.bodyAsText()}")
            e.printStackTrace()
            emptyList()
        }
    }
}