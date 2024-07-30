package com.melonhead.data_user.services

import com.melonhead.data_authentication.models.AuthToken
import com.melonhead.data_core_manga.models.Chapter
import com.melonhead.data_user.models.UserResponse
import com.melonhead.data_user.routes.HttpRoutes
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.extensions.catching
import com.melonhead.lib_networking.models.handlePagination
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

interface UserService {
    suspend fun getFollowedChapters(token: AuthToken): List<Chapter>

    suspend fun getInfo(token: AuthToken): UserResponse?
}

internal class UserServiceImpl(
    private val client: HttpClient
): UserService {
    override suspend fun getFollowedChapters(token: AuthToken): List<Chapter> {
        Clog.i("getFollowedChapters")
        return handlePagination(50, fetchAll = false) { offset ->
            client.catching("getFollowedChapters") {
                client.get(HttpRoutes.USER_FOLLOW_CHAPTERS_URL) {
                    headers {
                        contentType(ContentType.Application.Json)
                        bearerAuth(token.session)
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

    override suspend fun getInfo(token: AuthToken): UserResponse? {
        Clog.i("Get user")
        return client.catching("getInfo") {
            client.get(HttpRoutes.USER_ME_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token.session)
                }
            }
        }
    }
}
