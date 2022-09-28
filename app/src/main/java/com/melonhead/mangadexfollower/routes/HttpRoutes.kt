package com.melonhead.mangadexfollower.routes

object HttpRoutes {
    private const val BASE_URL = "https://api.mangadex.org"

    private const val AUTH_ROUTE = "${BASE_URL}/auth"
    private const val USER_ROUTE = "${BASE_URL}/user"

    const val LOGIN_URL = "${AUTH_ROUTE}/login"
    const val CHECK_TOKEN_URL = "${AUTH_ROUTE}/check"
    const val REFRESH_TOKEN_URL = "${AUTH_ROUTE}/refresh"

    const val MANGA_URL = "${BASE_URL}/manga"
    const val USER_FOLLOW_CHAPTERS_URL = "${USER_ROUTE}/follows/manga/feed"


}