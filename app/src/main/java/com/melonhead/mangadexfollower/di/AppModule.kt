package com.melonhead.mangadexfollower.di

import androidx.room.Room
import com.melonhead.mangadexfollower.db.chapter.ChapterDatabase
import com.melonhead.mangadexfollower.db.manga.MangaDatabase
import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.services.*
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient(CIO) {
            install(Logging) {
                level = LogLevel.INFO
                logger = object: Logger {
                    override fun log(message: String) {
                        Clog.i(message)
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<AppDataService> {
        AppDataServiceImpl(get())
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

    single<CoverService> {
        CoverServiceImpl(get())
    }

    factory { CoroutineScope(Dispatchers.IO) }

    single {
        MangaRepository(get(), get(), get(), get(), get(), get(), get(), get())
    }

    single(createdAtStart = true) {
        AuthRepository(get(), get(), get())
    }

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            ChapterDatabase::class.java, "chapter"
        ).build()
    }

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            MangaDatabase::class.java, "manga"
        ).build()
    }

    single {
        get<MangaDatabase>().mangaDao()
    }

    single {
        get<ChapterDatabase>().chapterDao()
    }

    viewModel {
        MainViewModel(get(), get(), get())
    }
}