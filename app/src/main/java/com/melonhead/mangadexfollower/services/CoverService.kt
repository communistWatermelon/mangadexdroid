package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.extensions.catching
import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.cover.Cover
import com.melonhead.mangadexfollower.models.shared.handlePagination
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

interface CoverService {
    suspend fun getCovers(token: AuthToken, mangaIds: List<String>): List<Cover>
}

class CoverServiceImpl(
    private val client: HttpClient
): CoverService {
    override suspend fun getCovers(token: AuthToken, mangaIds: List<String>): List<Cover> {
        Clog.i("getCovers: ${mangaIds.count()} $token")
        return handlePagination(mangaIds.count()) { offset ->
            client.catching("getCovers") {
                client.get(HttpRoutes.COVER_URL) {
                    headers {
                        contentType(ContentType.Application.Json)
                        bearerAuth(token.session)
                    }
                    url {
                        // TODO: prevent manga from being too long
                        mangaIds.forEach { encodedParameters.append("manga[]", it) }
                        parameters.append("limit", "100")
                        parameters.append("offset", "$offset")
                    }
                }
            }!!
        }
    }
}