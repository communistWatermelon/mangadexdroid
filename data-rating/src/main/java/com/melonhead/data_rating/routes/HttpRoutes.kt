package com.melonhead.data_rating.routes

import com.melonhead.lib_networking.routes.HttpRoutes.BASE_URL


internal object HttpRoutes {
    private const val BASE_RATING_ROUTE = "$BASE_URL/rating"

    const val RATING_URL = BASE_RATING_ROUTE

    const val ID_PLACEHOLDER = "{id}"
    const val RATING_CHANGE_URL = "$BASE_RATING_ROUTE/$ID_PLACEHOLDER"

}
