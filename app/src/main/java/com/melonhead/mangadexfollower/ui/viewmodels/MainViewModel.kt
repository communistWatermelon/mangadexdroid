package com.melonhead.mangadexfollower.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    mangaRepository: MangaRepository
): ViewModel() {
    val loginStatus = authRepository.loginStatus.asLiveData()
    val manga = mangaRepository.manga.asLiveData()

    fun authenticate(email: String, password: String) = viewModelScope.launch {
        authRepository.authenticate(email, password)
    }
}