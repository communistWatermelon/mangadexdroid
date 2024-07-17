package com.melonhead.mangadexfollower.services

import com.melonhead.mangadexfollower.extensions.catching
import com.melonhead.lib_logging.Clog
import com.melonhead.mangadexfollower.models.auth.AuthRequest
import com.melonhead.mangadexfollower.models.auth.AuthResponse
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.auth.RefreshTokenRequest
import com.melonhead.mangadexfollower.routes.HttpRoutes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

interface LoginService {
    suspend fun authenticate(email: String, password: String): AuthToken?
    suspend fun refreshToken(token: AuthToken, logoutOnFail: Boolean): AuthToken?
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

    override suspend fun refreshToken(token: AuthToken, logoutOnFail: Boolean): AuthToken? {
        return try {
            // note: not using catching call intentionally, prevents networking errors from forcing logout
            val result = client.post(HttpRoutes.REFRESH_TOKEN_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token.session)
                }
                setBody(RefreshTokenRequest(token.refresh))
            }
            val response: AuthResponse? = result.body()
            if (response?.result == "ok") response.token else null
        } catch (e: Exception) {
            com.melonhead.lib_logging.Clog.w(e.localizedMessage ?: "Unknown error")
            if (logoutOnFail) null else token
        }
    }
}
