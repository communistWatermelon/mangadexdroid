package com.melonhead.mangadexfollower.db.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseDbUser(
    val installDateSeconds: Long? = null,
    val lastRefreshDateSeconds: Long? = null,
    // TODO: add useful settings for user here
)