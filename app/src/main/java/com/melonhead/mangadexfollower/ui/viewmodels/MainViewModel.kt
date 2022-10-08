package com.melonhead.mangadexfollower.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.melonhead.mangadexfollower.extensions.asLiveData
import com.melonhead.mangadexfollower.models.ui.UIChapter
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val mangaRepository: MangaRepository
): ViewModel() {
    val loginStatus = authRepository.loginStatus.asLiveData()
    val manga = mangaRepository.manga.map {
        mutableIsLoading.value = it.isEmpty()
        it
    }.asLiveData()

    private val mutableIsLoading = MutableLiveData(true)
    val isLoading = mutableIsLoading.asLiveData()

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
        mutableIsLoading.value = true
        mangaRepository.forceRefresh()
        // this sucks, but the current architecture isn't great for
        delay(5000)
        mutableIsLoading.value = false
    }
}