package com.melonhead.feature_authentication.di

import com.melonhead.data_app_data.di.AppDataServiceModule
import com.melonhead.data_authentication.di.DataAuthenticationModule
import com.melonhead.data_user.di.UserServiceModule
import com.melonhead.feature_authentication.AuthRepository
import com.melonhead.feature_authentication.AuthRepositoryImpl
import org.koin.dsl.module

val FeatureAuthenticationModule = module {
    includes(DataAuthenticationModule)
    includes(AppDataServiceModule)
    includes(UserServiceModule)
    single<AuthRepository>(createdAtStart = true) { AuthRepositoryImpl(get(), get(), get(), get(), get()) }
}
