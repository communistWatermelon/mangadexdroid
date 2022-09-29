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

    val lastNotifyMs: Flow<Long>
    suspend fun updateLastNotifyMs(timeMs: Long)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AppDataServiceImpl(
    private val appContext: Context
): AppDataService {
    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val LAST_REFRESH_MS = longPreferencesKey("last_refresh_ms")
    private val LAST_NOTIFY_MS = longPreferencesKey("last_notify_ms")

    private val authTokenFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[AUTH_TOKEN] ?: ""
    }.distinctUntilChanged()

    private val refreshTokenFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[REFRESH_TOKEN] ?: ""
    }.distinctUntilChanged()

    override val lastRefreshMs: Flow<Long> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[LAST_REFRESH_MS] ?: 0L
    }.distinctUntilChanged()

    override val lastNotifyMs: Flow<Long> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[LAST_NOTIFY_MS] ?: 0L
    }.distinctUntilChanged()

    override var token: Flow<AuthToken?> = authTokenFlow.combine(refreshTokenFlow) { auth, refresh ->
        if (auth.isBlank() || refresh.isBlank()) return@combine null
        AuthToken(auth, refresh)
    }.distinctUntilChanged()

    override suspend fun updateToken(token: AuthToken?) {
        appContext.dataStore.edit { settings ->
            if (settings[AUTH_TOKEN] != token?.session)
                settings[AUTH_TOKEN] = token?.session ?: ""

            if (settings[REFRESH_TOKEN] != token?.refresh)
                settings[REFRESH_TOKEN] = token?.refresh ?: ""
        }
    }

    override suspend fun updateLastRefreshMs(timeMs: Long) {
        appContext.dataStore.edit { settings ->
            settings[LAST_REFRESH_MS] = timeMs
        }
    }

    override suspend fun updateLastNotifyMs(timeMs: Long) {
        appContext.dataStore.edit { settings ->
            settings[LAST_REFRESH_MS] = timeMs
        }
    }
}