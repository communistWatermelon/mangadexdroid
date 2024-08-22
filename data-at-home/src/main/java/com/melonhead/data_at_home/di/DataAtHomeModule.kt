package com.melonhead.data_at_home.di

import com.melonhead.data_app_data.di.DataAppDataModule
import com.melonhead.data_at_home.AtHomeService
import com.melonhead.data_at_home.AtHomeServiceImpl
import com.melonhead.lib_networking.di.LibNetworkingModule
import org.koin.dsl.module

val DataAtHomeModule = module {
    includes(LibNetworkingModule)
    includes(DataAppDataModule)
    single<AtHomeService> {
        AtHomeServiceImpl(get(), get())
    }
}
