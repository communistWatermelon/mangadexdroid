package com.melonhead.data_authentication.services

import com.melonhead.data_authentication.models.AuthRequest
import com.melonhead.data_authentication.models.AuthResponse
import com.melonhead.data_authentication.models.AuthToken
import com.melonhead.data_authentication.models.RefreshTokenRequest
import com.melonhead.data_authentication.routes.HttpRoutes.LOGIN_URL
import com.melonhead.data_authentication.routes.HttpRoutes.REFRESH_TOKEN_URL
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.extensions.catching
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface LoginService {
    suspend fun authenticate(email: String, password: String): AuthToken?
    suspend fun refreshToken(token: AuthToken, logoutOnFail: Boolean): AuthToken?
}

internal class LoginServiceImpl(
    private val client: HttpClient,
) : LoginService {
    override suspend fun authenticate(email: String, password: String): AuthToken? {
        val response: AuthResponse? = client.catching("authenticate") {
            client.post(LOGIN_URL) {
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
            val result = client.post(REFRESH_TOKEN_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(token.session)
                }
                setBody(RefreshTokenRequest(token.refresh))
            }
            val response: AuthResponse? = result.body()
            if (response?.result == "ok") response.token else null
        } catch (e: Exception) {
            Clog.w(e.localizedMessage ?: "Unknown error")
            if (logoutOnFail) null else token
        }
    }
}
