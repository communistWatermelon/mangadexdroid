package com.melonhead.mangadexfollower.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.melonhead.mangadexfollower.models.auth.AuthToken
import kotlinx.coroutines.flow.*

interface AppDataService {
    val token: Flow<AuthToken?>
    suspend fun updateToken(token: AuthToken?)

    val lastRefreshMs: Flow<Long>
    suspend fun updateLastRefreshMs(timeMs: Long)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AppDataServiceImpl(
    private val appContext: Context
): AppDataService {
    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val LAST_REFRESH_MS = longPreferencesKey("last_refresh_ms")

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

    override val lastRefreshMs: Flow<Long> = appContext.dataStore.data
        .map { preferences ->
            // No type safety.
            preferences[LAST_REFRESH_MS] ?: 0L
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

    override suspend fun updateLastRefreshMs(timeMs: Long) {
        appContext.dataStore.edit { settings ->
            settings[LAST_REFRESH_MS] = timeMs
        }
    }
}