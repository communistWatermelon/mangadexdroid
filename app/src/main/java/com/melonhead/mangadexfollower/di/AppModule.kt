package com.melonhead.mangadexfollower.di

import com.melonhead.mangadexfollower.routes.*
import com.melonhead.mangadexfollower.services.InMemoryTokenProvider
import com.melonhead.mangadexfollower.services.TokenProviderService
import com.melonhead.mangadexfollower.viewmodels.MainViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
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

    viewModel {
        MainViewModel(get(), get())
    }
}