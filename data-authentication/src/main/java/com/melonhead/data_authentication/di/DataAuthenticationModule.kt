package com.melonhead.data_authentication.di

import com.melonhead.lib_app_data.di.LibAppDataModule
import com.melonhead.data_authentication.services.LoginService
import com.melonhead.data_authentication.services.LoginServiceImpl
import com.melonhead.lib_networking.di.LibNetworkingModule
import org.koin.dsl.module

val DataAuthenticationModule = module {
    includes(LibNetworkingModule)
    includes(LibAppDataModule)
    single<LoginService> {
        LoginServiceImpl(get(), get())
    }
}
