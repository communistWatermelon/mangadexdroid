package com.melonhead.feature_webview_chapter_viewer.di

import com.melonhead.feature_webview_chapter_viewer.navigation.WebViewChapterViewerActivityResolver
import com.melonhead.feature_webview_chapter_viewer.viewmodels.WebViewViewModel
import com.melonhead.lib_app_events.di.LibAppEventsModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureWebViewChapterViewerModule = module {
    includes(LibAppEventsModule)

    viewModel { WebViewViewModel(get()) }

    single { WebViewChapterViewerActivityResolver() }
}
