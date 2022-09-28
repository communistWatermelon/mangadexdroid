package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.models.auth.AuthRequest
import com.melonhead.mangadexfollower.models.auth.AuthResponse
import com.melonhead.mangadexfollower.models.auth.CheckTokenResponse
import com.melonhead.mangadexfollower.models.auth.RefreshTokenRequest
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

interface LoginService {
    suspend fun authenticate(email: String, password: String): Boolean
    suspend fun isTokenValid(): Boolean
    suspend fun refreshToken(): Boolean
}

class LoginServiceImpl(
    private val client: HttpClient,
    private val tokenProvider: TokenProviderService
) : LoginService {
    override suspend fun authenticate(email: String, password: String): Boolean {
        val result = client.post(HttpRoutes.LOGIN_URL) {
            headers {
                contentType(ContentType.Application.Json)
            }
            setBody(AuthRequest(email, password))
        }

        val body: AuthResponse = result.body()
        return if (body.result == "ok") {
            tokenProvider.token = body.token
            true
        } else {
            false
        }
    }

    override suspend fun isTokenValid(): Boolean {
        val token = tokenProvider.token ?: return false
        val result: CheckTokenResponse = client.get(HttpRoutes.CHECK_TOKEN_URL ) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
        }.body()
        return result.isAuthenticated
    }

    override suspend fun refreshToken(): Boolean {
        val token = tokenProvider.token ?: return false
        val result: CheckTokenResponse = client.post(HttpRoutes.REFRESH_TOKEN_URL) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
            setBody(RefreshTokenRequest(token.refresh))
        }.body()

        return result.isAuthenticated
    }
}

