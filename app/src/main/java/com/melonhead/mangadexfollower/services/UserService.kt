package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.extensions.catching
import com.melonhead.lib_logging.Clog
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.content.Chapter
import com.melonhead.mangadexfollower.models.shared.handlePagination
import com.melonhead.mangadexfollower.models.user.UserResponse
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

interface UserService {
    suspend fun getFollowedChapters(token: AuthToken): List<Chapter>

    suspend fun getInfo(token: AuthToken): UserResponse?
}

class UserServiceImpl(
    private val client: HttpClient
): UserService {
    override suspend fun getFollowedChapters(token: AuthToken): List<Chapter> {
        com.melonhead.lib_logging.Clog.i("getFollowedChapters")
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
        com.melonhead.lib_logging.Clog.i("Get user")
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
