package com.melonhead.data_authentication.di

import com.melonhead.data_authentication.services.LoginService
import com.melonhead.data_authentication.services.LoginServiceImpl
import com.melonhead.lib_networking.di.NetworkingModule
import org.koin.dsl.module

val DataAuthenticationModule = module {
    includes(NetworkingModule)
    single<LoginService> {
        LoginServiceImpl(get())
    }
}
