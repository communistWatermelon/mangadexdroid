package com.melonhead.mangadexfollower.models

import com.melonhead.mangadexfollower.models.content.Chapter

data class UIManga(val id: String, val title: String, val chapters: MutableList<Chapter>)