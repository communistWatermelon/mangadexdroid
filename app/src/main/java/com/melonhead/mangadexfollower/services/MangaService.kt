package com.melonhead.mangadexfollower.services

import com.melonhead.data_authentication.models.AuthToken
import com.melonhead.lib_networking.extensions.catching
import com.melonhead.lib_networking.extensions.catchingSuccess
import com.melonhead.lib_logging.Clog
import com.melonhead.data_core_manga.models.Manga
import com.melonhead.mangadexfollower.models.content.MangaReadMarkersResponse
import com.melonhead.mangadexfollower.models.content.ReadChapterRequest
import com.melonhead.lib_networking.models.handlePagination
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.models.ui.UIManga
import com.melonhead.mangadexfollower.routes.HttpRoutes.ID_PLACEHOLDER
import com.melonhead.mangadexfollower.routes.HttpRoutes.MANGA_READ_CHAPTER_MARKERS_URL
import com.melonhead.mangadexfollower.routes.HttpRoutes.MANGA_READ_MARKERS_URL
import com.melonhead.mangadexfollower.routes.HttpRoutes.MANGA_URL
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*


interface MangaService {
    suspend fun getManga(token: AuthToken, mangaIds: List<String>): List<Manga>
    suspend fun getReadChapters(token: AuthToken, mangaIds: List<String>): List<String>
    suspend fun changeReadStatus(token: AuthToken, uiManga: UIManga, uiChapter: UIChapter, readStatus: Boolean)
}

class MangaServiceImpl(
    private val client: HttpClient,
): MangaService {
    override suspend fun getManga(token: AuthToken, mangaIds: List<String>): List<Manga> {
        Clog.i("getManga: ${mangaIds.count()}")
        val result: List<Manga?> = handlePagination(mangaIds.count()) { offset ->
            client.catching("getManga") {
                client.get(MANGA_URL) {
                    headers {
                        contentType(ContentType.Application.Json)
                        bearerAuth(token.session)
                    }
                    url {
                        // TODO: prevent ids from being too long
                        mangaIds.forEach { encodedParameters.append("ids[]", it) }
                        parameters.append("offset", offset.toString())
                        parameters.append("includes[]", "cover_art")
                    }
                }
            }
        }
        return result.filterNotNull()
    }

    override suspend fun getReadChapters(token: AuthToken, mangaIds: List<String>): List<String> {
        Clog.i("getReadChapters: total ${mangaIds.count()}")
        val allChapters = mutableListOf<String>()
        mangaIds.chunked(100).map { list ->
            Clog.i("getReadChapters: chunked ${list.count()}")
            val result: MangaReadMarkersResponse? = client.catching("getReadChapters") {
                client.get(MANGA_READ_MARKERS_URL) {
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
            }
            val chapters = result?.data ?: emptyList()
            allChapters.addAll(chapters)
        }
        return allChapters
    }

    override suspend fun changeReadStatus(token: AuthToken, uiManga: UIManga, uiChapter: UIChapter, readStatus: Boolean) {
        Clog.i("changeReadStatus: chapter ${uiChapter.title} readStatus $readStatus")
        client.catchingSuccess("changeReadStatus") {
            client.post(MANGA_READ_CHAPTER_MARKERS_URL.replace(ID_PLACEHOLDER, uiManga.id)) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token.session)
                }
                setBody(ReadChapterRequest.from(uiChapter, readStatus))
            }
        }
    }
}
