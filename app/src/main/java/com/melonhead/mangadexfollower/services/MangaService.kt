package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.content.Manga
import com.melonhead.mangadexfollower.models.content.MangaReadMarkersResponse
import com.melonhead.mangadexfollower.models.shared.handlePagination
import com.melonhead.mangadexfollower.routes.HttpRoutes.MANGA_READ_MARKERS_URL
import com.melonhead.mangadexfollower.routes.HttpRoutes.MANGA_URL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay


interface MangaService {
    suspend fun getManga(token: AuthToken, mangaIds: List<String>): List<Manga>
    suspend fun getReadChapters(mangaIds: List<String>, token: AuthToken): List<String>
}

class MangaServiceImpl(
    private val client: HttpClient,
): MangaService {
    override suspend fun getManga(token: AuthToken, mangaIds: List<String>): List<Manga> {
        Clog.i("getManga: ${mangaIds.count()} $mangaIds")
        return handlePagination(mangaIds.count()) { offset ->
            client.get(MANGA_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token.session)
                }
                url {
                    // TODO: prevent ids from being too long
                    mangaIds.forEach { encodedParameters.append("ids[]", it) }
                    parameters.append("offset", offset.toString())
                }
            }
        }
    }

    override suspend fun getReadChapters(mangaIds: List<String>, token: AuthToken): List<String> {
        Clog.i("getReadChapters: total ${mangaIds.count()} - $mangaIds")
        val allChapters = mutableListOf<String>()
        mangaIds.chunked(100).map { list ->
            Clog.i("getReadChapters: chunked ${list.count()}, $list")
            val result = client.get(MANGA_READ_MARKERS_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token.session)
                }
                url {
                    list.forEach {
                        encodedParameters.append("ids[]", it)
                    }
                }
            }

            val chapters = try {
                result.body<MangaReadMarkersResponse>().data
            } catch (e: Exception) {
                Clog.e("getReadChapters: ${result.bodyAsText()}", e)
                emptyList()
            }
            allChapters.addAll(chapters)
            // prevents triggering the anti-spam
            delay(250L)
        }
        return allChapters
    }
}