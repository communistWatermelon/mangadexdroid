package com.melonhead.mangadexfollower.ui.scenes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.melonhead.mangadexfollower.models.ui.*
import com.melonhead.mangadexfollower.notifications.NewChapterNotification
import com.melonhead.mangadexfollower.ui.scenes.home.HomeScreen
import com.melonhead.mangadexfollower.ui.scenes.login.LoginScreen
import com.melonhead.mangadexfollower.ui.scenes.shared.LoadingScreen
import com.melonhead.mangadexfollower.ui.theme.MangadexFollowerTheme
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // do nothing for now
        }

        // request permission to post notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MangadexFollowerTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val loginStatus by viewModel.loginStatus.observeAsState()
                    val manga by viewModel.manga.observeAsState(listOf())
                    val refreshStatus by viewModel.refreshStatus.observeAsState(None)
                    val refreshText by viewModel.refreshText.observeAsState("")
                    val context = LocalContext.current

                    when (loginStatus) {
                        com.melonhead.feature_authentication.models.LoginStatus.LoggedIn -> {
                            if (manga.isEmpty()) {
                                LoadingScreen(refreshStatus)
                            } else {
                                HomeScreen(
                                    manga,
                                    readMangaCount = viewModel.readMangaCount,
                                    refreshText = refreshText,
                                    refreshStatus = refreshStatus,
                                    onChapterClicked = { uiManga, chapter -> viewModel.onChapterClicked(context, uiManga, chapter) },
                                    onToggleChapterRead = { uiManga, uiChapter -> viewModel.toggleChapterRead(uiManga, uiChapter) },
                                    onSwipeRefresh = { viewModel.refreshContent() },
                                    onToggleMangaRenderType = { uiManga -> viewModel.toggleMangaWebview(uiManga) },
                                    onChangeMangaTitle = { uiManga, title ->  viewModel.setMangaTitle(uiManga, title) },
                                )
                            }
                        }
                        com.melonhead.feature_authentication.models.LoginStatus.LoggedOut -> LoginScreen { username, password -> viewModel.authenticate(username, password) }
                        com.melonhead.feature_authentication.models.LoginStatus.LoggingIn, null -> LoadingScreen(null)
                    }
                }
            }
        }

        onNewIntent(intent)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val mangaJson = intent.getStringExtra(NewChapterNotification.MANGA_EXTRA) ?: return
        val chapterJson = intent.getStringExtra(NewChapterNotification.CHAPTER_EXTRA) ?: return
        val manga: UIManga = Json.decodeFromString(mangaJson)
        val chapter: UIChapter = Json.decodeFromString(chapterJson)
        viewModel.onChapterClicked(this, manga, chapter)
    }
}
