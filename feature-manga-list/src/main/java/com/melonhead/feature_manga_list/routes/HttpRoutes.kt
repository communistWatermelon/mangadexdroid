package com.melonhead.feature_manga_list.routes

import com.melonhead.lib_networking.routes.HttpRoutes.BASE_URL

internal object HttpRoutes {
    private const val MANGA_ROUTE = "$BASE_URL/manga"

    const val MANGA_URL = "$MANGA_ROUTE/"
    const val MANGA_READ_MARKERS_URL = "$MANGA_ROUTE/read"

    const val ID_PLACEHOLDER = "{id}"
    const val MANGA_READ_CHAPTER_MARKERS_URL = "$MANGA_ROUTE/$ID_PLACEHOLDER/read"

}
