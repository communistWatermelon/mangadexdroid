package com.melonhead.feature_manga_list.di

import com.melonhead.data_app_data.di.DataAppDataModule
import com.melonhead.data_at_home.di.DataAtHomeModule
import com.melonhead.feature_manga_list.MangaRepository
import com.melonhead.feature_manga_list.MangaRepositoryImpl
import com.melonhead.feature_manga_list.services.MangaService
import com.melonhead.feature_manga_list.services.MangaServiceImpl
import com.melonhead.data_user.di.DataUserModule
import com.melonhead.lib_chapter_cache.di.LibChapterCacheModule
import com.melonhead.feature_manga_list.navigation.MangaListScreenResolver
import com.melonhead.feature_manga_list.viewmodels.MangaListViewModel
import com.melonhead.lib_app_context.di.LibAppContextModule
import com.melonhead.lib_app_events.di.LibAppEventsModule
import com.melonhead.lib_database.di.LibDbModule
import com.melonhead.lib_networking.di.LibNetworkingModule
import com.melonhead.lib_notifications.di.LibNotificationsModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureMangaListModule = module {
    includes(LibAppEventsModule)
    includes(LibNotificationsModule)
    includes(LibAppContextModule)
    includes(LibDbModule)
    includes(LibChapterCacheModule)
    includes(LibNetworkingModule)

    includes(DataUserModule)
    includes(DataAppDataModule)
    includes(DataAtHomeModule)

    single<MangaService> {
        MangaServiceImpl(get(), get())
    }

    single<MangaRepository> {
        MangaRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    viewModel { MangaListViewModel(get(), get(), get(), get()) }

    single<MangaListScreenResolver>(createdAtStart = true) { MangaListScreenResolver() }
}
