package com.melonhead.lib_navigation

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.melonhead.lib_navigation.keys.ActivityKey
import com.melonhead.lib_navigation.keys.ScreenKey
import com.melonhead.lib_navigation.resolvers.ActivityResolver
import com.melonhead.lib_navigation.resolvers.ResolverMap
import com.melonhead.lib_navigation.resolvers.ScreenResolver

interface Navigator {
    fun <T: ActivityKey> intentForKey(context: Context, activityKey: T): Intent

    @Composable
    fun <T: ScreenKey> ComposeWithKey(screenKey: T)
}

internal class NavigatorImpl(
    private val resolverMap: ResolverMap,
): Navigator {
    override fun <T: ActivityKey> intentForKey(context: Context, activityKey: T): Intent {
        val resolver = resolverMap.activityResolvers[activityKey::class.java]
            ?: throw IllegalArgumentException("ActivityResolver not registered for $activityKey")
        val activityResolver = (resolver as? ActivityResolver<T>) ?: throw IllegalArgumentException("Incorrect type registered for $activityKey, expecting ActivityResolver<${activityKey::class.java}>, got ${resolver::class.java}")
        return activityResolver.intentForKey(context, activityKey)
    }

    @Composable
    override fun <T: ScreenKey> ComposeWithKey(screenKey: T) {
        val resolver = resolverMap.screenResolvers[screenKey::class.java]
            ?: throw IllegalArgumentException("ScreenResolver not registered for $screenKey")
        val screenResolver = (resolver as? ScreenResolver<T>) ?: throw IllegalArgumentException("Incorrect type registered for $screenKey, expecting ScreenResolver<${screenKey::class.java}>, got ${resolver::class.java}")
        screenResolver.ComposeWithKey(screenKey)
    }
}
