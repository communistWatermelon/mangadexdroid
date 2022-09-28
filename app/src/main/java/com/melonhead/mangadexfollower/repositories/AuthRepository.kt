package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.services.LoginService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class AuthRepository(
    private val loginService: LoginService,
    private val externalScope: CoroutineScope
) {
    private val mutableIsLoggedIn = MutableStateFlow(false)
    val isLoggedIn = mutableIsLoggedIn.shareIn(externalScope, replay = 1, started = SharingStarted.WhileSubscribed())

    init {
        externalScope.launch { checkAuthentication() }
    }

    private suspend fun checkAuthentication() {
        var validToken = loginService.isTokenValid()
        if (!validToken) {
            validToken = loginService.refreshToken()
        }
        mutableIsLoggedIn.value = validToken
    }

    suspend fun authenticate(email: String, password: String) {
        loginService.authenticate(email, password)
        checkAuthentication()
    }
}