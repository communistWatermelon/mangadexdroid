package com.melonhead.lib_navigation.resolvers

import androidx.compose.runtime.Composable
import com.melonhead.lib_navigation.keys.ScreenKey

interface ScreenResolver<T: ScreenKey> {
    @Composable
    fun ComposeWithKey(key: T)
}
