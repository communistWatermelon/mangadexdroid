package com.melonhead.lib_app_data

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.melonhead.lib_app_data.models.RenderStyle
import com.melonhead.lib_database.extensions.addValueEventListenerFlow
import com.melonhead.lib_database.firebase.FirebaseDbUser
import dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

interface AppData {
    val token: Flow<Pair<String, String>?>
    val installDateSeconds: Flow<Long?>
    val lastRefreshDateSeconds: Flow<Long?>
    val userIdFlow: Flow<String?>
    val autoMarkMangaCompleted: Flow<Boolean>

    val renderStyle: RenderStyle
    val useDataSaver: Boolean
    val chapterTapAreaSize: Dp
    val showReadChapterCount: Int

    suspend fun updateToken(session: String?, refresh: String?)
    suspend fun updateInstallTime()
    suspend fun updateLastRefreshDate()
    suspend fun updateUserId(id: String)
    suspend fun updateRenderStyle(renderStyle: RenderStyle)
    suspend fun updateAutoMarkMangaCompleted(autoMarkMangaCompleted: Boolean)
    suspend fun setUseDataSaver(useDataSaver: Boolean)
    suspend fun setShowReadChapterCount(readChapterCount: Int)
    suspend fun getToken(): Pair<String, String>?
    suspend fun getSession(): String?
    suspend fun getRefresh(): String?
}

internal class AppDataImpl(
    private val appContext: Context,
    externalScope: CoroutineScope,
): AppData {
    private val firebaseDb = Firebase.database

    private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val USER_ID = stringPreferencesKey("user_id")

    private var hasFetchedDbUser = false

    private val tokenMutex = Mutex()

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

    private val authTokenFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        // No type safety.
        preferences[AUTH_TOKEN] ?: ""
    }.distinctUntilChanged()

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

    override val autoMarkMangaCompleted: Flow<Boolean> = currentFirebaseDBUser.map {
        it?.autoMarkMangaCompleted ?: true
    }.distinctUntilChanged()

    override var token: Flow<Pair<String, String>?> = authTokenFlow.combine(refreshTokenFlow) { auth, refresh ->
        if (auth.isBlank() || refresh.isBlank()) return@combine null
        auth to refresh
    }.distinctUntilChanged()

    override val renderStyle: RenderStyle
        get() = RenderStyle.Native

    override val useDataSaver: Boolean
        get() = false

    override val chapterTapAreaSize: Dp
        get() = 60.dp

    override val showReadChapterCount: Int
        get() = 1

    override suspend fun updateToken(session: String?, refresh: String?) {
        tokenMutex.withLock {
            appContext.dataStore.edit { settings ->
                if (settings[AUTH_TOKEN] != session)
                    settings[AUTH_TOKEN] = session ?: ""

                if (settings[REFRESH_TOKEN] != refresh)
                    settings[REFRESH_TOKEN] = refresh ?: ""
            }
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

    override suspend fun updateAutoMarkMangaCompleted(autoMarkMangaCompleted: Boolean) {
        val userDb = userDb() ?: return
        val new = currentDbUser().copy(autoMarkMangaCompleted = autoMarkMangaCompleted)
        userDb.setValue(new)
    }

    override suspend fun updateUserId(id: String) {
        appContext.dataStore.edit { settings ->
            settings[USER_ID] = id
        }
    }

    override suspend fun getToken(): Pair<String, String>? {
        tokenMutex.withLock {
            return token.first()
        }
    }

    override suspend fun getSession(): String? {
        tokenMutex.withLock {
            return token.first()?.first
        }
    }

    override suspend fun getRefresh(): String? {
        tokenMutex.withLock {
            return token.first()?.second
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
