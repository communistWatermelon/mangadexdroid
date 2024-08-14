package com.melonhead.feature_webview_chapter_viewer.di

import com.melonhead.data_manga.di.DataMangaModule
import com.melonhead.feature_webview_chapter_viewer.WebViewChapterViewerActivityResolver
import com.melonhead.feature_webview_chapter_viewer.WebViewViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val FeatureWebViewChapterViewerModule = module {
    includes(DataMangaModule)

    viewModel { WebViewViewModel(get()) }

    single { WebViewChapterViewerActivityResolver() }
}
