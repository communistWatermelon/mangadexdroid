package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.models.auth.*
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

interface LoginService {
    suspend fun authenticate(email: String, password: String): AuthToken?
    suspend fun isTokenValid(token: AuthToken): Boolean
    suspend fun refreshToken(token: AuthToken): AuthToken?
}

class LoginServiceImpl(
    private val client: HttpClient,
) : LoginService {
    override suspend fun authenticate(email: String, password: String): AuthToken? {
        val result = client.post(HttpRoutes.LOGIN_URL) {
            headers {
                contentType(ContentType.Application.Json)
            }
            setBody(AuthRequest(email, password))
        }

        val body: AuthResponse = result.body()
        return if (body.result == "ok") {
            body.token
        } else {
            null
        }
    }

    override suspend fun isTokenValid(token: AuthToken): Boolean {
        val result: CheckTokenResponse = client.get(HttpRoutes.CHECK_TOKEN_URL ) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
        }.body()
        return result.isAuthenticated
    }

    override suspend fun refreshToken(token: AuthToken): AuthToken? {
        val body: AuthResponse = client.post(HttpRoutes.REFRESH_TOKEN_URL) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
            setBody(RefreshTokenRequest(token.refresh))
        }.body()

        return if (body.result == "ok") {
            body.token
        } else {
            null
        }
    }
}

