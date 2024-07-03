package com.melonhead.mangadexfollower.services

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
enum class RenderStyle {
    Native, WebView, Browser
}

interface AppDataService {
    val token: Flow<AuthToken?>
    val installDateSeconds: Flow<Long?>
    val lastRefreshDateSeconds: Flow<Long?>
    val userIdFlow: Flow<String?>

    val renderStyle: RenderStyle
    val useDataSaver: Boolean
    val chapterTapAreaSize: Dp
    val showReadChapterCount: Int

    suspend fun updateToken(token: AuthToken?)
    suspend fun updateInstallTime()
    suspend fun updateLastRefreshDate()
    suspend fun updateUserId(id: String)
    suspend fun updateRenderStyle(renderStyle: RenderStyle)
    suspend fun setUseDataSaver(useDataSaver: Boolean)
    suspend fun setShowReadChapterCount(readChapterCount: Int)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AppDataServiceImpl(
    private val appContext: Context,
    externalScope: CoroutineScope,
): AppDataService {
    private val firebaseDb = Firebase.database

    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val USER_ID = stringPreferencesKey("user_id")

    private var hasFetchedDbUser = false

    private val authTokenFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[AUTH_TOKEN] ?: ""
    }.distinctUntilChanged()

    init {
        externalScope.launch(IO) {
            delay(100L)
            try {
                userIdFlow.collectLatest {
                    val userDb = userDb()
                    if (userDb != null) {
                        userDb.addValueEventListenerFlow(FirebaseDbUser::class.java).collectLatest { user ->
                            mutableCurrentFirebaseDBUser.value = user
                            if (user == null) updateInstallTime()
                            hasFetchedDbUser = true
                        }
                    } else {
                        mutableCurrentFirebaseDBUser.value = null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    override val renderStyle: RenderStyle
        get() = RenderStyle.Native

    override val useDataSaver: Boolean
        get() = false

    override val chapterTapAreaSize: Dp
        get() = 60.dp

    override val showReadChapterCount: Int
        get() = 1

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
        if (userId.isBlank()) return null
        return firebaseDb.getReference("users").child(userId)
    }

    private fun currentDbUser(): FirebaseDbUser {
        return currentFirebaseDBUser.value ?: FirebaseDbUser()
    }

    override suspend fun updateInstallTime() {
        val userDb = userDb() ?: return
        val new = currentDbUser()
        if (new.installDateSeconds != null || !hasFetchedDbUser) return
        userDb.setValue(new.copy(installDateSeconds = Clock.System.now().epochSeconds))
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

    override suspend fun updateRenderStyle(renderStyle: RenderStyle) {
        TODO("Not yet implemented")
    }

    override suspend fun setUseDataSaver(useDataSaver: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun setShowReadChapterCount(readChapterCount: Int) {
        TODO("Not yet implemented")
    }
}