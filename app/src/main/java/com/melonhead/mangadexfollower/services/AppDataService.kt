package com.melonhead.mangadexfollower.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.melonhead.mangadexfollower.db.firebase.FirebaseDbUser
import com.melonhead.mangadexfollower.extensions.addValueEventListenerFlow
import com.melonhead.mangadexfollower.models.auth.AuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

interface AppDataService {
    val token: Flow<AuthToken?>
    val installDateSeconds: Flow<Long?>
    val lastRefreshDateSeconds: Flow<Long?>
    val userIdFlow: Flow<String?>

    suspend fun updateToken(token: AuthToken?)
    suspend fun updateInstallTime()
    suspend fun updateLastRefreshDate()
    suspend fun updateUserId(id: String)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AppDataServiceImpl(
    private val appContext: Context,
    externalScope: CoroutineScope,
): AppDataService {
    private val firebaseDb = Firebase.database

    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val INSTALL_DATE_SECONDS = longPreferencesKey("install_epoch_seconds") // only for migration, shouldn't need for long
    private val REFRESH_DATE_SECONDS = longPreferencesKey("last_refresh_epoch_seconds") // only for migration, shouldn't need for long
    private val USER_ID = stringPreferencesKey("user_id")

    private val authTokenFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[AUTH_TOKEN] ?: ""
    }.distinctUntilChanged()

    init {
        externalScope.launch(IO) {
            userIdFlow.collectLatest {
                val userDb = userDb()
                if (userDb != null) {
                    userDb.addValueEventListenerFlow(FirebaseDbUser::class.java).collectLatest { user ->
                        mutableCurrentFirebaseDBUser.value = user
                    }
                } else {
                    mutableCurrentFirebaseDBUser.value = null
                }
            }
        }
    }

    private val refreshTokenFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[REFRESH_TOKEN] ?: ""
    }.distinctUntilChanged()

    override val userIdFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[USER_ID] ?: ""
    }.distinctUntilChanged()

    private val mutableCurrentFirebaseDBUser = MutableStateFlow<FirebaseDbUser?>(null)
    private val currentFirebaseDBUser = mutableCurrentFirebaseDBUser.asStateFlow()

    override val installDateSeconds: Flow<Long?> = currentFirebaseDBUser.map {
        it?.installDateSeconds
    }.distinctUntilChanged()

    override val lastRefreshDateSeconds: Flow<Long?> = currentFirebaseDBUser.map {
        it?.lastRefreshDateSeconds
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

    private suspend fun userDb(): DatabaseReference? {
        val userId = userIdFlow.firstOrNull() ?: return null
        return firebaseDb.getReference("users").child(userId)
    }

    private suspend fun currentDbUser(): FirebaseDbUser {
        val now = Clock.System.now().epochSeconds
        val installDate = appContext.dataStore.data.map { preferences ->
            preferences[INSTALL_DATE_SECONDS]
        }.firstOrNull()
        val refreshDate = appContext.dataStore.data.map { preferences ->
            preferences[REFRESH_DATE_SECONDS]
        }.firstOrNull()
        return currentFirebaseDBUser.value ?: FirebaseDbUser(installDateSeconds = installDate ?: now, lastRefreshDateSeconds = refreshDate ?: now)
    }

    override suspend fun updateInstallTime() {
        val userDb = userDb() ?: return
        val new = currentDbUser().copy(installDateSeconds =  Clock.System.now().epochSeconds)
        userDb.setValue(new)
    }
    override suspend fun updateLastRefreshDate() {
        val userDb = userDb() ?: return
        val new = currentDbUser().copy(lastRefreshDateSeconds = Clock.System.now().epochSeconds)
        userDb.setValue(new)
    }

    override suspend fun updateUserId(id: String) {
        appContext.dataStore.edit { settings ->
            settings[USER_ID] = id
        }
    }
}