package com.melonhead.mangadexfollower.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.melonhead.mangadexfollower.models.auth.AuthToken
import kotlinx.coroutines.flow.*

interface TokenProviderService {
    val token: Flow<AuthToken?>
    suspend fun updateToken(token: AuthToken?)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class SharedPreferencesTokenProvider(
    private val appContext: Context
): TokenProviderService {
    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    private val authTokenFlow: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            // No type safety.
            preferences[AUTH_TOKEN] ?: ""
        }

    private val refreshTokenFlow: Flow<String> = appContext.dataStore.data
        .map { preferences ->
            // No type safety.
            preferences[REFRESH_TOKEN] ?: ""
        }

    override var token: Flow<AuthToken?> = authTokenFlow.combine(refreshTokenFlow) { auth, refresh ->
        if (auth.isBlank() || refresh.isBlank()) return@combine null
        AuthToken(auth, refresh)
    }

    override suspend fun updateToken(token: AuthToken?) {
        appContext.dataStore.edit { settings ->
            settings[AUTH_TOKEN] = token?.session ?: ""
            settings[REFRESH_TOKEN] = token?.refresh ?: ""
        }
    }
}