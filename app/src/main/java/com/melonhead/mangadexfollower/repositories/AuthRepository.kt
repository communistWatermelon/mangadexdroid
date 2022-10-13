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
    val loginStatus = mutableIsLoggedIn.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch {
            appDataService.token.collectLatest {
                if (it != null) {
                    checkAuthentication(it)
                }
            }
        }
    }
    suspend fun checkCurrentAuthentication() {
        val token = appDataService.token.firstOrNull()
        checkAuthentication(token)
    }

    suspend fun isTokenValid(token: AuthToken?): Boolean {
        token ?: return false
        return loginService.isTokenValid(token)
    }

    private suspend fun checkAuthentication(token: AuthToken?) {
        Clog.i("checkAuthentication")
        mutableIsLoggedIn.value = LoginStatus.LoggingIn
        var currentToken: AuthToken? = token
        if (currentToken == null) {
            mutableIsLoggedIn.value = LoginStatus.LoggedOut
            appDataService.updateToken(null)
            return
        }
        val validToken = isTokenValid(currentToken)
        if (!validToken) {
            Clog.i("Token isn't valid, refreshing")
            currentToken = loginService.refreshToken(currentToken)
        }
        appDataService.updateToken(currentToken)
        mutableIsLoggedIn.value = if (validToken) LoginStatus.LoggedIn else LoginStatus.LoggedOut
    }

    suspend fun authenticate(email: String, password: String) {
        Clog.i("authenticate")
        val token = loginService.authenticate(email, password)
        checkAuthentication(token)
    }
}