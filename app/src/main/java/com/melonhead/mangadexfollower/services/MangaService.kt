package com.melonhead.mangadexfollower.services

import android.util.Log
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.content.Manga
import com.melonhead.mangadexfollower.models.content.MangaReadMarkersResponse
import com.melonhead.mangadexfollower.models.content.MangaResponse
import com.melonhead.mangadexfollower.routes.HttpRoutes.MANGA_READ_MARKERS_URL
import com.melonhead.mangadexfollower.routes.HttpRoutes.MANGA_URL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


interface MangaService {
    suspend fun getManga(mangaIds: List<String>): List<Manga>
    suspend fun getReadChapters(mangaIds: List<String>, token: AuthToken): List<String>
}

class MangaServiceImpl(
    private val client: HttpClient,
): MangaService {
    override suspend fun getManga(mangaIds: List<String>): List<Manga> {
        val result = client.get(MANGA_URL) {
            headers {
                contentType(ContentType.Application.Json)
            }
            url {
                mangaIds.forEach {
                    encodedParameters.append("ids[]", it)
                }
            }
        }

        return try {
            result.body<MangaResponse>().data
        } catch (e: Exception) {
            Log.w("", "getManga: ${result.bodyAsText()}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getReadChapters(mangaIds: List<String>, token: AuthToken): List<String> {
        val result = client.get(MANGA_READ_MARKERS_URL) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
            url {
                mangaIds.forEach {
                    encodedParameters.append("ids[]", it)
                }
            }
        }

        return try {
            result.body<MangaReadMarkersResponse>().data
        } catch (e: Exception) {
            Log.w("", "getReadChapters: ${result.bodyAsText()}")
            e.printStackTrace()
            emptyList()
        }
    }
}