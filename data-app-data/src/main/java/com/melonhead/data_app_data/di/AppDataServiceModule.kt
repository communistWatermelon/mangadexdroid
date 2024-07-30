package com.melonhead.data_app_data.di

import com.melonhead.data_app_data.AppDataService
import com.melonhead.data_app_data.AppDataServiceImpl
import org.koin.dsl.module

val AppDataServiceModule = module {
    single<AppDataService> {
        AppDataServiceImpl(get(), get())
    }
}
