package com.melonhead.mangadexfollower.repositories

import com.melonhead.mangadexfollower.services.MangaService
import com.melonhead.mangadexfollower.services.UserService

class MangaRepository(
    private val mangaService: MangaService,
    private val userService: UserService
) {

}