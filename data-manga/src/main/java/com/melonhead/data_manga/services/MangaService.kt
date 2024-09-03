package com.melonhead.data_manga.services

import com.melonhead.data_manga.models.*
import com.melonhead.data_manga.models.MangaReadMarkersResponse
import com.melonhead.data_manga.models.ReadChapterRequest
import com.melonhead.lib_app_data.AppData
import com.melonhead.data_shared.models.Manga
import com.melonhead.data_manga.routes.HttpRoutes.ID_PLACEHOLDER
import com.melonhead.data_manga.routes.HttpRoutes.MANGA_READ_CHAPTER_MARKERS_URL
import com.melonhead.data_manga.routes.HttpRoutes.MANGA_READ_MARKERS_URL
import com.melonhead.data_manga.routes.HttpRoutes.MANGA_READ_STATUS_URL
import com.melonhead.data_manga.routes.HttpRoutes.MANGA_URL
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.extensions.catching
import com.melonhead.lib_networking.extensions.catchingSuccess
import com.melonhead.lib_networking.models.handlePagination
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface MangaService {
    suspend fun getManga(mangaIds: List<String>): List<Manga>
    suspend fun getReadChapters(mangaIds: List<String>): List<String>
    suspend fun changeReadStatus(mangaId: String, chapterId: String, readStatus: Boolean)
    suspend fun getSeriesReadingStatus(mangaId: String): ReadingStatus?
    suspend fun changeSeriesReadingStatus(mangaId: String, readingStatus: ReadingStatus)
}

internal class MangaServiceImpl(
    private val client: HttpClient,
    private val appData: AppData,
): MangaService {
    override suspend fun getManga(mangaIds: List<String>): List<Manga> {
        val session = appData.getSession() ?: return emptyList()
        Clog.i("getManga: ${mangaIds.count()}")
        val result: List<Manga?> = handlePagination(mangaIds.count()) { offset ->
            client.catching("getManga") {
                client.get(MANGA_URL) {
                    headers {
                        contentType(ContentType.Application.Json)
                        bearerAuth(session)
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

    override suspend fun getReadChapters(mangaIds: List<String>): List<String> {
        val session = appData.getSession() ?: return emptyList()
        Clog.i("getReadChapters: total ${mangaIds.count()}")
        val allChapters = mutableListOf<String>()
        mangaIds.chunked(100).map { list ->
            Clog.i("getReadChapters: chunked ${list.count()}")
            val result: MangaReadMarkersResponse? = client.catching("getReadChapters") {
                client.get(MANGA_READ_MARKERS_URL) {
                    headers {
                        contentType(ContentType.Application.Json)
                        bearerAuth(session)
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

    override suspend fun changeReadStatus(mangaId: String, chapterId: String, readStatus: Boolean) {
        val session = appData.getSession() ?: return
        Clog.i("changeReadStatus: chapter $chapterId readStatus $readStatus")
        client.catchingSuccess("changeReadStatus") {
            client.post(MANGA_READ_CHAPTER_MARKERS_URL.replace(ID_PLACEHOLDER, mangaId)) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(session)
                }
                setBody(ReadChapterRequest.from(chapterId, readStatus))
            }
        }
    }

    override suspend fun getSeriesReadingStatus(mangaId: String): ReadingStatus? {
        val session = appData.getSession() ?: return null
        Clog.i("getSeriesReadingStatus: manga $mangaId")
        val result: ReadingStatusResponse? = client.catching("getSeriesReadingStatus") {
            client.get(MANGA_READ_STATUS_URL.replace(ID_PLACEHOLDER, mangaId)) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(session)
                }
            }
        }
        val status = result?.status
        return ReadingStatus.from(status)
    }

    override suspend fun changeSeriesReadingStatus(mangaId: String, readingStatus: ReadingStatus) {
        val session = appData.getSession() ?: return
        Clog.i("changeSeriesReadingStatus: manga $mangaId readingStatus $readingStatus")
        client.catchingSuccess("changeSeriesReadingStatus") {
            client.post(MANGA_READ_STATUS_URL.replace(ID_PLACEHOLDER, mangaId)) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(session)
                }
                setBody(ReadingStatusRequest.from(readingStatus))
            }
        }
    }
}
