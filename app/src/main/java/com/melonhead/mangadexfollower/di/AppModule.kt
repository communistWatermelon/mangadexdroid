package com.melonhead.mangadexfollower.di

import com.melonhead.data_app_data.di.AppDataServiceModule
import com.melonhead.feature_authentication.di.FeatureAuthenticationModule
import com.melonhead.feature_manga_list.di.FeatureMangaListModule
import com.melonhead.feature_native_chapter_viewer.di.FeatureNativeChapterViewerModule
import com.melonhead.feature_webview_chapter_viewer.di.FeatureWebViewChapterViewerModule
import com.melonhead.lib_app_events.di.LibAppEventsModule
import com.melonhead.lib_navigation.di.LibNavigationModule
import com.melonhead.mangadexfollower.AppNavigationMap
import com.melonhead.mangadexfollower.navigation.MainActivityResolver
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val AppModule = module {
    includes(LibAppEventsModule)
    includes(LibNavigationModule)

    includes(AppDataServiceModule)

    includes(FeatureAuthenticationModule)
    includes(FeatureMangaListModule)
    includes(FeatureNativeChapterViewerModule)
    includes(FeatureWebViewChapterViewerModule)

    viewModel {
        MainViewModel(get(), get())
    }

    single(createdAtStart = true) { MainActivityResolver() }
    single(createdAtStart = true) { AppNavigationMap(get(), get(), get(), get(), get(), get()) }
}
