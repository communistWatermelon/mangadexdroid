package com.melonhead.data_manga.di

import com.melonhead.data_app_data.di.DataAppDataModule
import com.melonhead.data_manga.services.MangaService
import com.melonhead.data_manga.services.MangaServiceImpl
import com.melonhead.lib_networking.di.LibNetworkingModule
import org.koin.dsl.module

val DataMangaModule = module {
    includes(LibNetworkingModule)
    includes(DataAppDataModule)
    single<MangaService> {
        MangaServiceImpl(get(), get())
    }
}
