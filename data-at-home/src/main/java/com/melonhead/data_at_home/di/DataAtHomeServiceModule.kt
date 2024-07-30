package com.melonhead.data_at_home.di

import com.melonhead.data_at_home.AtHomeService
import com.melonhead.data_at_home.AtHomeServiceImpl
import com.melonhead.lib_networking.di.NetworkingModule
import org.koin.dsl.module

val DataAtHomeServiceModule = module {
    includes(NetworkingModule)
    single<AtHomeService> {
        AtHomeServiceImpl(get())
    }
}
