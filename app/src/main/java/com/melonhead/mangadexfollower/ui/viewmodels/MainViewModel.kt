package com.melonhead.mangadexfollower.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.services.AppDataService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val mangaRepository: MangaRepository,
    userAppDataService: AppDataService
): ViewModel() {
    val loginStatus = authRepository.loginStatus.asLiveData()
    val manga = mangaRepository.manga.asLiveData()
    val refreshStatus = mangaRepository.refreshStatus.asLiveData()
    val lastRefreshDateSeconds = userAppDataService.lastRefreshDateSeconds.asLiveData()

    fun authenticate(email: String, password: String) = viewModelScope.launch {
        authRepository.authenticate(email, password)
    }

    fun onChapterClicked(context: Context, uiChapter: UIChapter) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uiChapter.webAddress)
        }
        context.startActivity(intent)
    }

    fun refreshContent() = viewModelScope.launch {
        mangaRepository.forceRefresh()
        // this sucks, but the current architecture isn't great for
        delay(5000)
    }
}