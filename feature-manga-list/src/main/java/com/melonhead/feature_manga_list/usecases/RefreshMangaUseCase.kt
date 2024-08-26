package com.melonhead.feature_manga_list.usecases

import com.melonhead.feature_manga_list.MangaRepository

@Deprecated("Use UserEvent.RefreshManga event instead of using this directly")
interface RefreshMangaUseCase {
    suspend fun invoke()
}

@Suppress("DEPRECATION") // This is the only valid use case of forceRefresh
internal class RefreshMangaUseCaseImpl(
    private val mangaRepository: MangaRepository,
): RefreshMangaUseCase {
    override suspend fun invoke() {
        mangaRepository.forceRefresh()
    }
}
