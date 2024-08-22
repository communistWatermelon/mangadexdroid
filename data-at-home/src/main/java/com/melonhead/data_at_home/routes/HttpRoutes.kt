package com.melonhead.data_at_home.routes

import com.melonhead.lib_networking.routes.HttpRoutes.BASE_URL

internal object HttpRoutes {
    private const val AT_HOME_ROUTE = "${BASE_URL}/at-home/server"
    const val CHAPTER_DATA_URL = "${AT_HOME_ROUTE}/"
}
