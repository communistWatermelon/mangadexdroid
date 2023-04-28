package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.extensions.catching
import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.content.AtHomeChapterResponse
import com.melonhead.mangadexfollower.routes.HttpRoutes.CHAPTER_DATA_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface AtHomeService {
    suspend fun getChapterData(authToken: AuthToken, chapterId: String): AtHomeChapterResponse?
}

class AtHomeServiceImpl(
    private val client: HttpClient,
): AtHomeService {
    override suspend fun getChapterData(
        authToken: AuthToken,
        chapterId: String
    ): AtHomeChapterResponse? {
        Clog.i("getChapterData: $chapterId")
        return client.catching("getChapterData") {
            client.get(CHAPTER_DATA_URL + chapterId) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(authToken.session)
                }
            }
        }
    }
}