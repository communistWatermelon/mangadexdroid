package com.melonhead.feature_authentication.navigation

import androidx.compose.runtime.Composable
import com.melonhead.feature_authentication.ui.scenes.OauthLoginScreen
import com.melonhead.lib_navigation.keys.ScreenKey
import com.melonhead.lib_navigation.resolvers.ScreenResolver

class OauthLoginScreenResolver: ScreenResolver<ScreenKey.OauthLoginScreen> {
    @Composable
    override fun ComposeWithKey(key: ScreenKey.OauthLoginScreen) {
        OauthLoginScreen(key.onLoginTapped, key.storedEmail, key.storedClientId, key.storedClientSecret)
    }
}
