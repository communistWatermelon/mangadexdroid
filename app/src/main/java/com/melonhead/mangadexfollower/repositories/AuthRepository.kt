package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.models.auth.AuthToken
import com.melonhead.mangadexfollower.services.LoginService
import com.melonhead.mangadexfollower.services.AppDataService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class AuthRepository(
    private val appDataService: AppDataService,
    private val loginService: LoginService,
    private val externalScope: CoroutineScope,
) {
    private val mutableIsLoggedIn = MutableStateFlow(false)
    val isLoggedIn = mutableIsLoggedIn.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch {
            appDataService.token.collectLatest {
                if (it != null) {
                    checkAuthentication(it)
                }
            }
        }
    }

    private suspend fun checkAuthentication(token: AuthToken?) {
        var currentToken: AuthToken? = token
        if (currentToken == null) {
            appDataService.updateToken(null)
            return
        }
        val validToken = loginService.isTokenValid(currentToken)
        if (!validToken) {
            currentToken = loginService.refreshToken(currentToken)
        }
        appDataService.updateToken(currentToken)
        mutableIsLoggedIn.value = validToken
    }

    suspend fun authenticate(email: String, password: String) {
        val token = loginService.authenticate(email, password)
        checkAuthentication(token)
    }
}