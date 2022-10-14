package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.extensions.catching
import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.models.auth.*
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
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
        val response: AuthResponse? = client.catching("authenticate") {
            client.post(HttpRoutes.LOGIN_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                }
                setBody(AuthRequest(email, password))
            }
        }
        return if (response?.result == "ok") response.token else null
    }

    override suspend fun isTokenValid(token: AuthToken): Boolean {
        val response: CheckTokenResponse? = client.catching("isTokenValid") {
            client.get(HttpRoutes.CHECK_TOKEN_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token.session)
                }
            }
        }

        val tokenIsValid = response?.isAuthenticated ?: false
        Clog.i("Token is valid: $tokenIsValid")
        return tokenIsValid
    }

    override suspend fun refreshToken(token: AuthToken): AuthToken? {
        val response: AuthResponse? = client.catching("refreshToken") {
            client.post(HttpRoutes.REFRESH_TOKEN_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token.session)
                }
                setBody(RefreshTokenRequest(token.refresh))
            }
        }
        return if (response?.result == "ok") response.token else null
    }
}

