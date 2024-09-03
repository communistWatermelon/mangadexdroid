package com.melonhead.lib_database.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseDbUser(
    val installDateSeconds: Long? = null,
    val lastRefreshDateSeconds: Long? = null,
    val autoMarkMangaCompleted: Boolean? = null,
    // TODO: add useful settings for user here
)
