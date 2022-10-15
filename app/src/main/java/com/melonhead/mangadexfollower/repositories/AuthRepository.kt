package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.models.ui.LoginStatus
import com.melonhead.mangadexfollower.services.AppDataService
import com.melonhead.mangadexfollower.services.LoginService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthRepository(
    private val appDataService: AppDataService,
    private val loginService: LoginService,
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

    suspend fun refreshToken(): AuthToken? {
        val currentToken = appDataService.token.firstOrNull() ?: return null
        val newToken = loginService.refreshToken(currentToken)
        appDataService.updateToken(currentToken)
        if (newToken == null) mutableIsLoggedIn.value = LoginStatus.LoggedOut
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