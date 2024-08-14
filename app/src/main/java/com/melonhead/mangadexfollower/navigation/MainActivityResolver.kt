package com.melonhead.mangadexfollower.navigation

import android.content.Context
import android.content.Intent
import com.melonhead.lib_navigation.keys.ActivityKey
import com.melonhead.lib_navigation.resolvers.ActivityResolver
import com.melonhead.mangadexfollower.ui.scenes.MainActivity

class MainActivityResolver: ActivityResolver<ActivityKey.MainActivity> {
    override fun intentForKey(context: Context, key: ActivityKey.MainActivity): Intent {
        return Intent(context, MainActivity::class.java)
    }
}
