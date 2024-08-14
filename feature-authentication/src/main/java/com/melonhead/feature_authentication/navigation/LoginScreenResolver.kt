package com.melonhead.feature_authentication.navigation

import androidx.compose.runtime.Composable
import com.melonhead.feature_authentication.ui.scenes.LoginScreen
import com.melonhead.lib_navigation.keys.ScreenKey
import com.melonhead.lib_navigation.resolvers.ScreenResolver

class LoginScreenResolver: ScreenResolver<ScreenKey.LoginScreen> {
    @Composable
    override fun ComposeWithKey(key: ScreenKey.LoginScreen) {
        LoginScreen(key.onLoginTapped)
    }
}
