package com.melonhead.data_user.services

import com.melonhead.lib_app_data.AppData
import com.melonhead.data_core_manga.models.Chapter
import com.melonhead.data_user.models.UserResponse
import com.melonhead.data_user.routes.HttpRoutes
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.extensions.catching
import com.melonhead.lib_networking.models.handlePagination
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface UserService {
    suspend fun getFollowedChapters(): List<Chapter>
    suspend fun getInfo(): UserResponse?
}

internal class UserServiceImpl(
    private val client: HttpClient,
    private val appData: AppData,
): UserService {
    override suspend fun getFollowedChapters(): List<Chapter> {
        val session = appData.getSession() ?: return emptyList()
        Clog.i("getFollowedChapters")
        return handlePagination(50, fetchAll = false) { offset ->
            client.catching("getFollowedChapters") {
                client.get(HttpRoutes.USER_FOLLOW_CHAPTERS_URL) {
                    headers {
                        contentType(ContentType.Application.Json)
                        bearerAuth(session)
                    }
                    url {
                        encodedParameters.append("translatedLanguage[]", "en")
                        parameters.append("order[createdAt]", "desc")
                        parameters.append("limit", "100")
                        parameters.append("offset", "$offset")
                    }
                }
            }
        }
    }

    override suspend fun getInfo(): UserResponse? {
        val session = appData.getSession() ?: return null
        Clog.i("Get user")
        return client.catching("getInfo") {
            client.get(HttpRoutes.USER_ME_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(session)
                }
            }
        }
    }
}
