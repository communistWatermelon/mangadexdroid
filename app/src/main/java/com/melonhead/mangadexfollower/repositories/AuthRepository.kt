package com.melonhead.mangadexfollower.repositories

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.melonhead.mangadexfollower.App
import com.melonhead.lib_logging.Clog
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.ui.LoginStatus
import com.melonhead.mangadexfollower.notifications.AuthFailedNotification
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.services.LoginService
import com.melonhead.mangadexfollower.services.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.RuntimeException

class AuthRepository(
    private val appContext: Context,
    private val appDataService: AppDataService,
    private val loginService: LoginService,
    private val userService: UserService,
    externalScope: CoroutineScope,
) {
    private val mutableIsLoggedIn = MutableStateFlow<LoginStatus>(LoginStatus.LoggedOut)
    val loginStatus = mutableIsLoggedIn.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed()).distinctUntilChanged()

    init {
        externalScope.launch {
            mutableIsLoggedIn.value = if (appDataService.token.firstOrNull() != null) LoginStatus.LoggedIn else LoginStatus.LoggedOut
            refreshToken()
        }
    }

    suspend fun refreshToken(logoutOnFail: Boolean = false): AuthToken? {
        suspend fun signOut() {
            Clog.e("Signing out, refresh failed", Exception())
            mutableIsLoggedIn.value = LoginStatus.LoggedOut
            if ((appContext as App).inForeground) return
            val notificationManager = NotificationManagerCompat.from(appContext)
            if (!notificationManager.areNotificationsEnabled()) return
            AuthFailedNotification.postAuthFailed(appContext)
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
        }
        return newToken
    }
    suspend fun authenticate(email: String, password: String) {
        Clog.i("authenticate")
        mutableIsLoggedIn.value = LoginStatus.LoggingIn
        val token = loginService.authenticate(email, password)
        appDataService.updateToken(token)
        mutableIsLoggedIn.value = LoginStatus.LoggedIn
    }
}
