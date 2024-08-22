package com.melonhead.feature_authentication

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.melonhead.lib_app_data.AppData
import com.melonhead.data_authentication.models.AuthToken
import com.melonhead.data_authentication.services.LoginService
import com.melonhead.data_user.services.UserService
import com.melonhead.lib_app_context.AppContext
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.AuthenticationEvent
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_notifications.AuthFailedNotificationChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface AuthRepository {
    suspend fun authenticate(email: String, password: String)
}

internal class AuthRepositoryImpl(
    private val context: Context,
    private val appData: AppData,
    private val loginService: LoginService,
    private val userService: UserService,
    private val appEventsRepository: AppEventsRepository,
    private val authFailedNotificationChannel: AuthFailedNotificationChannel,
    private val appContext: AppContext,
    externalScope: CoroutineScope,
) : AuthRepository {
    init {
        externalScope.launch {
            appEventsRepository.postEvent(if (appData.token.firstOrNull() != null) AuthenticationEvent.LoggedIn else AuthenticationEvent.LoggedOut)
            refreshToken(logoutOnFail = false)
        }

        externalScope.launch(context = Dispatchers.IO) {
            appEventsRepository.events.collectLatest {
                launch {
                    if (it is AuthenticationEvent.RefreshToken) refreshToken(logoutOnFail = it.logoutOnFail)
                }
            }
        }
    }

    private suspend fun refreshToken(logoutOnFail: Boolean): AuthToken? {
        suspend fun signOut() {
            Clog.e("Signing out, refresh failed", Exception())
            appEventsRepository.postEvent(AuthenticationEvent.LoggedOut)
            if (appContext.isInForeground) return
            val notificationManager = NotificationManagerCompat.from(context)
            if (!notificationManager.areNotificationsEnabled()) return
            authFailedNotificationChannel.postAuthFailed(context)
            appData.updateUserId("")
        }

        val currentToken = appData.token.firstOrNull()
        if (currentToken == null) {
            signOut()
            return null
        }

        val newToken = loginService.refreshToken(logoutOnFail)
        appData.updateToken(session = newToken?.session, refresh = newToken?.refresh)
        if (newToken == null) {
            signOut()
        } else {
            val userResponse = userService.getInfo()
            val userId = userResponse?.data?.id
            if (userId == null) {
                Clog.i("userResponse = ${userResponse?.toString()}")
                Clog.e("User info returned null", RuntimeException("User info returned null"))
            }
            appData.updateUserId(userId ?: "")
            appEventsRepository.postEvent(AuthenticationEvent.LoggedIn)
        }
        return newToken
    }

    override suspend fun authenticate(email: String, password: String) {
        Clog.i("authenticate")
        appEventsRepository.postEvent(AuthenticationEvent.LoggingIn)
        val token = loginService.authenticate(email, password)
        appData.updateToken(session = token?.session, refresh = token?.refresh)
        appEventsRepository.postEvent(AuthenticationEvent.LoggedIn)
        appEventsRepository.postEvent(UserEvent.RefreshManga)
    }
}
