package com.melonhead.data_at_home

import com.melonhead.data_app_data.AppDataService
import com.melonhead.data_authentication.models.AuthToken
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.extensions.catching
import com.melonhead.lib_networking.routes.HttpRoutes.BASE_URL
import com.melonhead.data_at_home.models.AtHomeChapterResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface AtHomeService {
    suspend fun getChapterData(chapterId: String): AtHomeChapterResponse?
}

private const val AT_HOME_ROUTE = "${BASE_URL}/at-home/server"
private const val CHAPTER_DATA_URL = "${AT_HOME_ROUTE}/"

internal class AtHomeServiceImpl(
    private val client: HttpClient,
    private val appDataService: AppDataService,
): AtHomeService {
    override suspend fun getChapterData(chapterId: String): AtHomeChapterResponse? {
        val authToken = appDataService.getToken() ?: return null
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
