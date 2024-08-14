package com.melonhead.data_authentication.routes

import com.melonhead.lib_networking.routes.HttpRoutes.BASE_URL

internal object HttpRoutes {
    private const val AUTH_ROUTE = "${BASE_URL}/auth"
    const val LOGIN_URL = "${AUTH_ROUTE}/login"
    const val REFRESH_TOKEN_URL = "${AUTH_ROUTE}/refresh"
}
