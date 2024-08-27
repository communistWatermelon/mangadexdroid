package com.melonhead.feature_manga_list.di

import com.melonhead.data_at_home.di.DataAtHomeModule
import com.melonhead.data_manga.di.DataMangaModule
import com.melonhead.data_user.di.DataUserModule
import com.melonhead.feature_manga_list.MangaRepository
import com.melonhead.feature_manga_list.MangaRepositoryImpl
import com.melonhead.feature_manga_list.navigation.MangaListScreenResolver
import com.melonhead.feature_manga_list.viewmodels.MangaListViewModel
import com.melonhead.lib_app_context.di.LibAppContextModule
import com.melonhead.lib_app_data.di.LibAppDataModule
import com.melonhead.lib_app_events.di.LibAppEventsModule
import com.melonhead.lib_chapter_cache.di.LibChapterCacheModule
import com.melonhead.lib_database.di.LibDbModule
import com.melonhead.lib_notifications.di.LibNotificationsModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureMangaListModule = module {
    includes(LibAppEventsModule)
    includes(LibNotificationsModule)
    includes(LibAppContextModule)
    includes(LibDbModule)
    includes(LibChapterCacheModule)
    includes(LibAppDataModule)

    includes(DataUserModule)
    includes(DataAtHomeModule)
    includes(DataMangaModule)

    single<MangaRepository>(createdAtStart = true) {
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
