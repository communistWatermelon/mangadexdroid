package com.melonhead.mangadexfollower.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val mangaRepository: MangaRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    val isLoggedIn = authRepository.isLoggedIn.asLiveData()
    val manga = mangaRepository.manga.asLiveData()
    // TODO: show loading status

    fun authenticate(email: String, password: String) = viewModelScope.launch {
        authRepository.authenticate(email, password)
    }
}