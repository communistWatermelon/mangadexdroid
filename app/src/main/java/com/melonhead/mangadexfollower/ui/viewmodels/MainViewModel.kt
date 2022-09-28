package com.melonhead.mangadexfollower.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.melonhead.mangadexfollower.extensions.asLiveData
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val mangaRepository: MangaRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val mutableIsLoggedIn = MutableLiveData(false)
    val isLoggedIn = mutableIsLoggedIn.asLiveData()

    private val mutableChapters = MutableLiveData<List<String>>(listOf())
    val chapters = mutableChapters.asLiveData()

    val manga = mangaRepository.manga.asLiveData()

    fun authenticate(email: String, password: String) = viewModelScope.launch {
        authRepository.authenticate(email, password)
        mutableIsLoggedIn.value = authRepository.isLoggedIn.replayCache.firstOrNull() ?: false
    }
}