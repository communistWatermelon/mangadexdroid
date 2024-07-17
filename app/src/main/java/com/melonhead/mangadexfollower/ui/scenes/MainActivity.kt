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
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.core.content.ContextCompat
import com.melonhead.mangadexfollower.models.ui.*
import com.melonhead.mangadexfollower.notifications.NewChapterNotification
import com.melonhead.mangadexfollower.ui.scenes.home.HomeList
import com.melonhead.mangadexfollower.ui.scenes.home.LoginScreen
import com.melonhead.mangadexfollower.ui.scenes.home.MainContent
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
                    MainContent(viewModel)
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
