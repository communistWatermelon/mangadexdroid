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
import kotlinx.datetime.Clock

interface AppDataService {
    val token: Flow<AuthToken?>
    val installDateSeconds: Flow<Long?>
    suspend fun updateToken(token: AuthToken?)
    suspend fun updateInstallTime()
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AppDataServiceImpl(
    private val appContext: Context
): AppDataService {
    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val INSTALL_DATE_SECONDS = longPreferencesKey("install_epoch_seconds")

    private val authTokenFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[AUTH_TOKEN] ?: ""
    }.distinctUntilChanged()

    private val refreshTokenFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[REFRESH_TOKEN] ?: ""
    }.distinctUntilChanged()

    override val installDateSeconds: Flow<Long?> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[INSTALL_DATE_SECONDS]
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

    override suspend fun updateInstallTime() {
        if (installDateSeconds.firstOrNull() == null) {
            appContext.dataStore.edit { settings ->
                settings[INSTALL_DATE_SECONDS] = Clock.System.now().epochSeconds
            }
        }
    }
}