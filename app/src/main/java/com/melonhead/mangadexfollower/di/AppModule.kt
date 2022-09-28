package com.melonhead.mangadexfollower.di

import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.services.*
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<TokenProviderService> {
        InMemoryTokenProvider()
    }

    single<LoginService> {
        LoginServiceImpl(get(), get())
    }

    single<UserService> {
        UserServiceImpl(get(), get())
    }

    single<MangaService> {
        MangaServiceImpl(get())
    }

    factory { CoroutineScope(Dispatchers.IO) }

    single {
        MangaRepository(get(), get(), get(), get(), get(named("loginFlow")))
    }

    single(createdAtStart = true) {
        AuthRepository(get(), get())
    }

    single(named("loginFlow")) {
        get<AuthRepository>().isLoggedIn
    }

    viewModel {
        MainViewModel(get(), get())
    }
}