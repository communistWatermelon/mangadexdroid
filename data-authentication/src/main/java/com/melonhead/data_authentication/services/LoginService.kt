package com.melonhead.data_authentication.services

import com.melonhead.data_authentication.models.*
import com.melonhead.data_authentication.models.AuthRequest
import com.melonhead.data_authentication.models.AuthResponse
import com.melonhead.data_authentication.models.RefreshTokenRequest
import com.melonhead.lib_app_data.AppData
import com.melonhead.data_authentication.routes.HttpRoutes.LOGIN_URL
import com.melonhead.data_authentication.routes.HttpRoutes.OAUTH_LOGIN_URL
import com.melonhead.data_authentication.routes.HttpRoutes.OAUTH_REFRESH_TOKEN_URL
import com.melonhead.data_authentication.routes.HttpRoutes.REFRESH_TOKEN_URL
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.extensions.catching
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.parametersOf

interface LoginService {
    suspend fun authenticateOauth(email: String, password: String, apiClient: String, apiSecret: String): OAuthToken?
    @Deprecated("No longer supported, use oauth variant instead")
    suspend fun authenticate(email: String, password: String): AuthToken?
    @Deprecated("No longer supported, use oauth variant instead")
    suspend fun refreshToken(logoutOnFail: Boolean): AuthToken?
    suspend fun refreshOAuthToken(logoutOnFail: Boolean, email: String, apiClient: String, apiSecret: String): OAuthToken?
}

internal class LoginServiceImpl(
    private val client: HttpClient,
    private val appData: AppData,
) : LoginService {
    override suspend fun authenticateOauth(
        email: String,
        password: String,
        apiClient: String,
        apiSecret: String
    ): OAuthToken? {
        val response: OAuthToken? = client.catching("oauth authenticate") {
            client.submitForm(OAUTH_LOGIN_URL, formParameters = Parameters.build {
                    append("grant_type", "password")
                    append("username", email)
                    append("password", password)
                    append("client_id", apiClient)
                    append("client_secret", apiSecret)
                }
            )
        }
        return response
    }

    @Deprecated("No longer supported, use apiClient variant instead")
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

    @Deprecated("No longer supported, use oauth variant instead")
    override suspend fun refreshToken(logoutOnFail: Boolean): AuthToken? {
        val (session, refresh) = appData.getToken() ?: return null
        return try {
            // note: not using catching call intentionally, prevents networking errors from forcing logout
            val result = client.post(REFRESH_TOKEN_URL) {
                headers {
                    contentType(ContentType.Application.Json)
                    bearerAuth(session)
                }
                setBody(RefreshTokenRequest(refresh))
            }
            val response: AuthResponse? = result.body()
            if (response?.result == "ok") response.token else null
        } catch (e: Exception) {
            Clog.w(e.localizedMessage ?: "Unknown error")
            if (logoutOnFail) null else AuthToken(session, refresh)
        }
    }

    override suspend fun refreshOAuthToken(logoutOnFail: Boolean, email: String, apiClient: String, apiSecret: String): OAuthToken? {
        val (session, refresh) = appData.getToken() ?: return null
        return try {
            // note: not using catching call intentionally, prevents networking errors from forcing logout
            val result = client.submitForm(OAUTH_REFRESH_TOKEN_URL, formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refresh)
                    append("client_id", apiClient)
                    append("client_secret", apiSecret)
                }
            )
            val response: OAuthRefreshResponse = result.body() ?: return null
            return OAuthToken(response.accessToken, refresh)
        } catch (e: Exception) {
            Clog.w(e.localizedMessage ?: "Unknown error")
            if (logoutOnFail) null else OAuthToken(session, refresh)
        }
    }
}
