package com.melonhead.mangadexfollower.models

import com.melonhead.mangadexfollower.models.content.Chapter

data class Manga(val id: String, val title: String, val chapters: List<Chapter>)