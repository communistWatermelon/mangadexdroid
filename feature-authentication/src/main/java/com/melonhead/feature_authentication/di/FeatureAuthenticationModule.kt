package com.melonhead.feature_authentication.di

import com.melonhead.data_app_data.di.AppDataServiceModule
import com.melonhead.data_authentication.di.DataAuthenticationModule
import com.melonhead.data_user.di.UserServiceModule
import com.melonhead.feature_authentication.AuthRepository
import com.melonhead.feature_authentication.AuthRepositoryImpl
import com.melonhead.feature_authentication.navigation.LoginScreenResolver
import com.melonhead.lib_app_context.di.LibAppContextModule
import com.melonhead.lib_app_events.di.LibAppEventsModule
import com.melonhead.lib_notifications.di.LibNotificationsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val FeatureAuthenticationModule = module {
    factory { CoroutineScope(Dispatchers.IO) }

    includes(LibAppEventsModule)
    includes(LibNotificationsModule)
    includes(LibAppContextModule)

    includes(DataAuthenticationModule)

    includes(AppDataServiceModule)
    includes(UserServiceModule)

    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get(), get(), get(), get(), get()) }
    single<LoginScreenResolver>(createdAtStart = true) { LoginScreenResolver() }
}
