package com.melonhead.mangadexfollower.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melonhead.data_shared.models.ui.UIChapter
import com.melonhead.data_shared.models.ui.UIManga
import com.melonhead.feature_authentication.AuthRepository
import com.melonhead.feature_authentication.models.LoginStatus
import com.melonhead.lib_app_data.AppData
import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.events.AuthenticationEvent
import com.melonhead.lib_app_events.events.UserEvent
import com.melonhead.lib_notifications.NewChapterNotificationChannel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainViewModel(
    private val authRepository: AuthRepository,
    private val appEventsRepository: AppEventsRepository,
    private val appData: AppData,
): ViewModel() {
    val loginStatus = appEventsRepository.events.mapNotNull {
        when (it) {
            is AuthenticationEvent.LoggedIn -> LoginStatus.LoggedIn
            is AuthenticationEvent.LoggedOut -> LoginStatus.LoggedOut
            is AuthenticationEvent.LoggingIn -> LoginStatus.LoggingIn
            else -> null
        }
    }.asLiveData(viewModelScope.coroutineContext)

    val clientDetails = flow<Triple<String, String, String>> {
        appData.getClient()
    }.asLiveData(viewModelScope.coroutineContext)

    @Deprecated("Use oauth variant")
    fun authenticate(email: String, password: String) = viewModelScope.launch {
        // TODO: replace with event?
        authRepository.authenticate(email, password)
    }

    fun authenticate(email: String, password: String, clientId: String, clientSecret: String) = viewModelScope.launch {
        // TODO: replace with event?
        authRepository.authenticate(email, password, clientId, clientSecret)
    }

    fun parseIntent(context: Context, intent: Intent) {
        val mangaJson = intent.getStringExtra(NewChapterNotificationChannel.MANGA_EXTRA) ?: return
        val chapterJson = intent.getStringExtra(NewChapterNotificationChannel.CHAPTER_EXTRA) ?: return
        val manga: UIManga = Json.decodeFromString(mangaJson)
        val chapter: UIChapter = Json.decodeFromString(chapterJson)
        appEventsRepository.postEvent(UserEvent.OpenedNotification(context, manga, chapter))
    }
}
