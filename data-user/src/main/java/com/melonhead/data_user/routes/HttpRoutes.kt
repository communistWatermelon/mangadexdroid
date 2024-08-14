package com.melonhead.data_user.routes

import com.melonhead.lib_networking.routes.HttpRoutes.BASE_URL

object HttpRoutes {
    private const val USER_ROUTE = "${BASE_URL}/user"
    const val USER_FOLLOW_CHAPTERS_URL = "${USER_ROUTE}/follows/manga/feed"
    const val USER_ME_URL = "${USER_ROUTE}/me"
}
