package com.melonhead.lib_app_context.di

import com.melonhead.lib_app_context.AppContext
import com.melonhead.lib_app_context.AppContextImpl
import org.koin.dsl.module

val LibAppContextModule = module {
    single<AppContext> { AppContextImpl() }
}
