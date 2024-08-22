package com.melonhead.lib_app_data.di

import com.melonhead.lib_app_data.AppData
import com.melonhead.lib_app_data.AppDataImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val LibAppDataModule = module {
    factory { CoroutineScope(Dispatchers.IO) }
    single<AppData> {
        AppDataImpl(get(), get())
    }
}
