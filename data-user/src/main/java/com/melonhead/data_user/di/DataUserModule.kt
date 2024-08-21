package com.melonhead.data_user.di

import com.melonhead.data_user.services.UserService
import com.melonhead.data_user.services.UserServiceImpl
import com.melonhead.lib_networking.di.LibNetworkingModule
import org.koin.dsl.module

val DataUserModule = module {
    includes(LibNetworkingModule)
    single<UserService> { UserServiceImpl(get()) }

}
