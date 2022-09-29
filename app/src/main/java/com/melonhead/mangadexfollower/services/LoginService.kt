package com.melonhead.mangadexfollower.services

import android.util.Log
import com.melonhead.mangadexfollower.models.auth.*
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

        return try {
            val body: AuthResponse = result.body()
            if (body.result == "ok") {
                body.token
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("", "authenticate: ${result.bodyAsText()}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun isTokenValid(token: AuthToken): Boolean {
        val result = client.get(HttpRoutes.CHECK_TOKEN_URL ) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
        }
        return try {
            result.body<CheckTokenResponse>().isAuthenticated
        } catch (e: Exception) {
            Log.w("", "isTokenValid: ${result.bodyAsText()}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun refreshToken(token: AuthToken): AuthToken? {
        val result = client.post(HttpRoutes.REFRESH_TOKEN_URL) {
            headers {
                contentType(ContentType.Application.Json)
                bearerAuth(token.session)
            }
            setBody(RefreshTokenRequest(token.refresh))
        }

        return try {
            val body = result.body<AuthResponse>()
            if (body.result == "ok") {
                body.token
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("", "refreshToken: ${result.bodyAsText()}")
            e.printStackTrace()
            null
        }
    }
}

