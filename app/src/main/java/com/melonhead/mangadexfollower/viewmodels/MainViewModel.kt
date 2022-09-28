package com.melonhead.mangadexfollower.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melonhead.mangadexfollower.extensions.asLiveData
import com.melonhead.mangadexfollower.routes.LoginService
import com.melonhead.mangadexfollower.routes.UserService
import kotlinx.coroutines.launch

class MainViewModel(
    private val loginService: LoginService,
    private val userService: UserService
): ViewModel() {
    private val mutableIsLoggedIn = MutableLiveData<Boolean>(false)
    val isLoggedIn = mutableIsLoggedIn.asLiveData()

    private val mutableChapters = MutableLiveData<List<String>>(listOf())
    val chapters = mutableChapters.asLiveData()

    init {
        getChapters()
    }

    fun authenticate(username: String, password: String) = viewModelScope.launch {
        val result = loginService.authenticate(username, password)
        mutableIsLoggedIn.value = result
        if (result) getChapters()
    }

    private suspend fun checkLogin(): Boolean {
        var validToken = loginService.isTokenValid()
        if (!validToken) {
            validToken = loginService.refreshToken()
        }
        mutableIsLoggedIn.value = validToken
        return validToken
    }

    private fun getChapters() = viewModelScope.launch {
        val loggedIn = checkLogin()
        if (!loggedIn) return@launch
        val chapters = userService.getFollowedChapters()
        mutableChapters.value = chapters.data.map { "${it.attributes.title} - ${it.attributes.chapter}" }
    }
}