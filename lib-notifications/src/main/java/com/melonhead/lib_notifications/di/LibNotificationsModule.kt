package com.melonhead.lib_notifications.di

import com.melonhead.lib_navigation.di.LibNavigationModule
import com.melonhead.lib_notifications.AuthFailedNotificationChannel
import com.melonhead.lib_notifications.NewChapterNotificationChannel
import org.koin.dsl.module

val LibNotificationsModule = module {
    includes(LibNavigationModule)

    single { NewChapterNotificationChannel(get()) }
    single { AuthFailedNotificationChannel(get()) }
}
