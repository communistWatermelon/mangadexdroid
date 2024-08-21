package com.melonhead.feature_native_chapter_viewer.di

import com.melonhead.data_app_data.di.DataAppDataModule
import com.melonhead.feature_native_chapter_viewer.viewmodels.ChapterViewModel
import com.melonhead.feature_native_chapter_viewer.navigation.NativeChapterViewerActivityResolver
import com.melonhead.lib_app_events.di.LibAppEventsModule
import com.melonhead.lib_navigation.di.LibNavigationModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureNativeChapterViewerModule = module {
    includes(LibAppEventsModule)
    includes(LibNavigationModule)
    includes(DataAppDataModule)

    viewModel {
        ChapterViewModel(get(), get(), get())
    }

    single { NativeChapterViewerActivityResolver() }
}
