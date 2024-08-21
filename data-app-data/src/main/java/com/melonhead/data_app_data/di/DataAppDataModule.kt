package com.melonhead.data_app_data.di

import com.melonhead.data_app_data.AppDataService
import com.melonhead.data_app_data.AppDataServiceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val DataAppDataModule = module {
    factory { CoroutineScope(Dispatchers.IO) }
    single<AppDataService> {
        AppDataServiceImpl(get(), get())
    }
}
