package com.melonhead.mangadexfollower.routes

object HttpRoutes {
    private const val BASE_URL = "https://api.mangadex.org"

    private const val MANGA_ROUTE = "${BASE_URL}/manga"

    const val MANGA_URL = "${MANGA_ROUTE}/"
    const val MANGA_READ_MARKERS_URL = "${MANGA_ROUTE}/read"

    const val ID_PLACEHOLDER = "{id}"
    const val MANGA_READ_CHAPTER_MARKERS_URL = "${MANGA_ROUTE}/$ID_PLACEHOLDER/read"



}
