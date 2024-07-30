package com.melonhead.data_user.di

import com.melonhead.data_user.services.UserService
import com.melonhead.data_user.services.UserServiceImpl
import com.melonhead.lib_networking.di.NetworkingModule
import org.koin.dsl.module

val UserServiceModule = module {
    includes(NetworkingModule)
    single<UserService> { UserServiceImpl(get()) }

}
