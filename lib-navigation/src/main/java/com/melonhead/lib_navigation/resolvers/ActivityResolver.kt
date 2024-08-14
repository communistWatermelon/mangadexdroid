package com.melonhead.lib_navigation.resolvers

import android.content.Context
import android.content.Intent
import com.melonhead.lib_navigation.keys.ActivityKey

interface ActivityResolver<T: ActivityKey> {
    fun intentForKey(context: Context, key: T): Intent
}
