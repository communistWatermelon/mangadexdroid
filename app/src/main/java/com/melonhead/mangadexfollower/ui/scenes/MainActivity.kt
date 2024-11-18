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
import androidx.core.content.ContextCompat
import com.melonhead.lib_core.scenes.LoadingScreen
import com.melonhead.lib_core.theme.MangadexFollowerTheme
import com.melonhead.feature_authentication.models.LoginStatus
import com.melonhead.lib_app_data.AppData
import com.melonhead.lib_navigation.Navigator
import com.melonhead.lib_navigation.keys.ScreenKey
import com.melonhead.mangadexfollower.BuildConfig
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val navigator: Navigator by inject()
    private val appData: AppData by inject()

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val loginStatus by viewModel.loginStatus.observeAsState()
                    val clientDetails by viewModel.clientDetails.observeAsState()

                    when (loginStatus) {
                        LoginStatus.LoggedIn -> {
                            navigator.ComposeWithKey(
                                screenKey = ScreenKey.MangaListScreen(
                                    buildVersionName = BuildConfig.VERSION_NAME,
                                    buildVersionCode = BuildConfig.VERSION_CODE.toString(),
                                )
                            )
                        }

                        LoginStatus.LoggedOut, null -> {
                            val (email, clientId, clientSecret) = clientDetails ?: Triple("", "", "")
                            navigator.ComposeWithKey(screenKey = ScreenKey.OauthLoginScreen(onLoginTapped = { username, password, clientId, clientSecret ->
                                viewModel.authenticate(
                                    username,
                                    password,
                                    clientId,
                                    clientSecret
                                )
                            },
                                email, clientId, clientSecret))
                        }

                        LoginStatus.LoggingIn -> LoadingScreen(null)
                    }
                }
            }
        }

        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.parseIntent(this, intent)
    }
}
