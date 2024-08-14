package com.melonhead.lib_app_events.di

import com.melonhead.lib_app_events.AppEventsRepository
import com.melonhead.lib_app_events.AppEventsRepositoryImpl
import org.koin.dsl.module

val LibAppEventsModule = module {
    single<AppEventsRepository> { AppEventsRepositoryImpl() }
}
