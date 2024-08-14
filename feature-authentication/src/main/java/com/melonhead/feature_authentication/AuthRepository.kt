package com.melonhead.feature_authentication

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.melonhead.data_app_data.AppDataService
import com.melonhead.data_authentication.models.AuthToken
import com.melonhead.data_authentication.services.LoginService
import com.melonhead.data_user.services.UserService
import com.melonhead.lib_app_context.AppContext
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.AuthenticationEvent
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_notifications.AuthFailedNotificationChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface AuthRepository {
    suspend fun authenticate(email: String, password: String)
}

internal class AuthRepositoryImpl(
    private val appContext: Context,
    private val appDataService: AppDataService,
    private val loginService: LoginService,
    private val userService: UserService,
    private val appEventsRepository: AppEventsRepository,
    private val authFailedNotificationChannel: AuthFailedNotificationChannel,
    externalScope: CoroutineScope,
) : AuthRepository {
    init {
        externalScope.launch {
            appEventsRepository.postEvent(if (appDataService.token.firstOrNull() != null) AuthenticationEvent.LoggedIn else AuthenticationEvent.LoggedOut)
            refreshToken(logoutOnFail = false)
        }

        externalScope.launch {
            appEventsRepository.events.collectLatest {
                if (it is AuthenticationEvent.RefreshToken) refreshToken(logoutOnFail = it.logoutOnFail)
            }
        }
    }

    private suspend fun refreshToken(logoutOnFail: Boolean): AuthToken? {
        suspend fun signOut() {
            Clog.e("Signing out, refresh failed", Exception())
            appEventsRepository.postEvent(AuthenticationEvent.LoggedOut)
            if (AppContext.isInForeground) return
            val notificationManager = NotificationManagerCompat.from(appContext)
            if (!notificationManager.areNotificationsEnabled()) return
            authFailedNotificationChannel.postAuthFailed(appContext)
            appDataService.updateUserId("")
        }

        val currentToken = appDataService.token.firstOrNull()
        if (currentToken == null) {
            signOut()
            return null
        }
        val newToken = loginService.refreshToken(currentToken, logoutOnFail)
        appDataService.updateToken(newToken)
        if (newToken == null) {
            signOut()
        } else {
            val userResponse = userService.getInfo(newToken)
            val userId = userResponse?.data?.id
            if (userId == null) {
                Clog.e("User info returned null", RuntimeException())
            }
            appDataService.updateUserId(userId ?: "")
            appEventsRepository.postEvent(AuthenticationEvent.LoggedIn)
        }
        return newToken
    }

    override suspend fun authenticate(email: String, password: String) {
        Clog.i("authenticate")
        appEventsRepository.postEvent(AuthenticationEvent.LoggingIn)
        val token = loginService.authenticate(email, password)
        appDataService.updateToken(token)
        appEventsRepository.postEvent(AuthenticationEvent.LoggedIn)
    }
}
