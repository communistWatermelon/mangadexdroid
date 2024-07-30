package com.melonhead.feature_authentication

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.melonhead.data_app_data.AppDataService
import com.melonhead.data_authentication.models.AuthToken
import com.melonhead.data_authentication.services.LoginService
import com.melonhead.data_user.services.UserService
import com.melonhead.feature_authentication.models.LoginStatus
import com.melonhead.lib_app_context.AppContext
import com.melonhead.lib_logging.Clog
//import com.melonhead.lib_notifications.AuthFailedNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface AuthRepository {
    suspend fun refreshToken(logoutOnFail: Boolean = false): AuthToken?
    suspend fun authenticate(email: String, password: String)
    val loginStatus: Flow<LoginStatus>
}

internal class AuthRepositoryImpl(
    private val appContext: Context,
    private val appDataService: AppDataService,
    private val loginService: LoginService,
    private val userService: UserService,
    externalScope: CoroutineScope,
) : AuthRepository {
    private val mutableIsLoggedIn = MutableStateFlow<LoginStatus>(LoginStatus.LoggedOut)
    override val loginStatus = mutableIsLoggedIn.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed()).distinctUntilChanged()

    init {
        externalScope.launch {
            mutableIsLoggedIn.value = if (appDataService.token.firstOrNull() != null) LoginStatus.LoggedIn else LoginStatus.LoggedOut
            refreshToken()
        }
    }

    override suspend fun refreshToken(logoutOnFail: Boolean): AuthToken? {
        suspend fun signOut() {
            Clog.e("Signing out, refresh failed", Exception())
            mutableIsLoggedIn.value = LoginStatus.LoggedOut
            if (AppContext.isInForeground) return
            val notificationManager = NotificationManagerCompat.from(appContext)
            if (!notificationManager.areNotificationsEnabled()) return
            // TODO: RESTORE THIS
//            AuthFailedNotification.postAuthFailed(appContext)
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

    override suspend fun authenticate(email: String, password: String) {
        Clog.i("authenticate")
        mutableIsLoggedIn.value = LoginStatus.LoggingIn
        val token = loginService.authenticate(email, password)
        appDataService.updateToken(token)
        mutableIsLoggedIn.value = LoginStatus.LoggedIn
    }
}
