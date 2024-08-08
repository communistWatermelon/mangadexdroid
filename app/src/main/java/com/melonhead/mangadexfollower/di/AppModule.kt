package com.melonhead.mangadexfollower.di

import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.services.*
import com.melonhead.mangadexfollower.ui.viewmodels.ChapterViewModel
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import com.melonhead.mangadexfollower.ui.viewmodels.WebViewViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<AppDataService> {
        AppDataServiceImpl(get(), get())
    }

    single<LoginService> {
        LoginServiceImpl(get())
    }

    single<UserService> {
        UserServiceImpl(get())
    }

    single<MangaService> {
        MangaServiceImpl(get())
    }

    single<AtHomeService> {
        AtHomeServiceImpl(get())
    }

    factory { CoroutineScope(Dispatchers.IO) }

    single {
        MangaRepository(get(), get(), get(), get(), get(), get(), get(), get(), get())
    }

    single(createdAtStart = true) {
        AuthRepository(get(), get(), get(), get(), get())
    }

    viewModel {
        MainViewModel(get(), get(), get())
    }

    viewModel {
        WebViewViewModel(get())
    }

    viewModel {
        ChapterViewModel(get(), get())
    }
}
