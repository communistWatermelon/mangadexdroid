package com.melonhead.data_rating.di

import com.melonhead.data_rating.services.RatingService
import com.melonhead.data_rating.services.RatingServiceImpl
import com.melonhead.lib_app_data.di.LibAppDataModule
import com.melonhead.lib_networking.di.LibNetworkingModule
import org.koin.dsl.module

val DataRatingModule = module {
    includes(LibNetworkingModule)
    includes(LibAppDataModule)
    single<RatingService> {
        RatingServiceImpl(get(), get())
    }
}
