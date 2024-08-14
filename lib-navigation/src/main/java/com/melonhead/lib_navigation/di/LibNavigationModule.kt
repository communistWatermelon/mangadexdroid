package com.melonhead.lib_navigation.di

import com.melonhead.lib_navigation.Navigator
import com.melonhead.lib_navigation.NavigatorImpl
import com.melonhead.lib_navigation.resolvers.ResolverMap
import com.melonhead.lib_navigation.resolvers.ResolverMapImpl
import org.koin.dsl.module

val LibNavigationModule = module {
    single<ResolverMap> { ResolverMapImpl() }
    single<Navigator> { NavigatorImpl(get()) }
}
