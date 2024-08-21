package com.melonhead.feature_manga_list.di

import com.melonhead.data_app_data.di.AppDataServiceModule
import com.melonhead.data_at_home.di.DataAtHomeServiceModule
import com.melonhead.feature_manga_list.MangaRepository
import com.melonhead.feature_manga_list.MangaRepositoryImpl
import com.melonhead.feature_manga_list.services.MangaService
import com.melonhead.feature_manga_list.services.MangaServiceImpl
import com.melonhead.data_user.di.UserServiceModule
import com.melonhead.feature_chapter_cache.di.ChapterCacheModule
import com.melonhead.feature_manga_list.navigation.MangaListScreenResolver
import com.melonhead.lib_app_events.di.LibAppEventsModule
import com.melonhead.lib_database.di.DBModule
import com.melonhead.lib_networking.di.NetworkingModule
import com.melonhead.lib_notifications.di.LibNotificationsModule
import org.koin.dsl.module

val FeatureMangaListModule = module {
    includes(NetworkingModule)
    includes(UserServiceModule)
    includes(AppDataServiceModule)
    includes(DataAtHomeServiceModule)
    includes(DBModule)
    includes(ChapterCacheModule)
    includes(LibAppEventsModule)
    includes(LibNotificationsModule)

    single<MangaService> {
        MangaServiceImpl(get())
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
        )
    }

    single<MangaListScreenResolver>(createdAtStart = true) { MangaListScreenResolver() }
}
