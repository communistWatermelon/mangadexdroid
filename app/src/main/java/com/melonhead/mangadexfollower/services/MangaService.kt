package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.models.content.Manga
import com.melonhead.mangadexfollower.models.content.MangaResponse
import com.melonhead.mangadexfollower.routes.HttpRoutes.MANGA_URL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*


interface MangaService {
    suspend fun getManga(id: String): Manga
}

class MangaServiceImpl(
    private val client: HttpClient,
): MangaService {
    override suspend fun getManga(id: String): Manga {
        val result = client.get(MANGA_URL) {
            headers {
                contentType(ContentType.Application.Json)
            }
            url {
                appendPathSegments(id)
            }
        }
        return result.body<MangaResponse>().data
    }
}