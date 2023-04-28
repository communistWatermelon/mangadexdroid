package com.melonhead.mangadexfollower.di

import androidx.room.Room
import com.melonhead.mangadexfollower.db.chapter.ChapterDatabase
import com.melonhead.mangadexfollower.db.manga.MangaDatabase
import com.melonhead.mangadexfollower.db.readmarkers.ReadMarkerDatabase
import com.melonhead.mangadexfollower.logs.Clog
import com.melonhead.mangadexfollower.ratelimit.RateLimit
import com.melonhead.mangadexfollower.ratelimit.impl.default
import com.melonhead.mangadexfollower.ratelimit.impl.rate
import com.melonhead.mangadexfollower.repositories.AuthRepository
import com.melonhead.mangadexfollower.repositories.MangaRepository
import com.melonhead.mangadexfollower.services.*
import com.melonhead.mangadexfollower.ui.viewmodels.ChapterViewModel
import com.melonhead.mangadexfollower.ui.viewmodels.MainViewModel
import com.melonhead.mangadexfollower.ui.viewmodels.WebViewViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.DurationUnit

val appModule = module {
    single {
        HttpClient(CIO) {
            install(RateLimit) {
                // globally set a 5 permit per 1 second rate-limiting
                default().rate(5, 1, DurationUnit.SECONDS)
            }
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
            install(HttpRequestRetry) {
                retryIf { _, response ->
                    !response.status.isSuccess() && response.status.value != 301 && response.status.value != 429
                }
                retryOnExceptionIf { _, cause ->
                    cause is ConnectTimeoutException || cause is JsonConvertException
                }
                exponentialDelay()
            }
        }
    }

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

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            ChapterDatabase::class.java, "chapter"
        ).fallbackToDestructiveMigration().build()
    }

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            MangaDatabase::class.java, "manga"
        ).fallbackToDestructiveMigration().build()
    }

    single(createdAtStart = true) {
        Room.databaseBuilder(
            get(),
            ReadMarkerDatabase::class.java, "readmarker"
        ).fallbackToDestructiveMigration().build()
    }

    single {
        get<MangaDatabase>().mangaDao()
    }

    single {
        get<ChapterDatabase>().chapterDao()
    }

    single {
        get<ReadMarkerDatabase>().readMarkersDao()
    }

    viewModel {
        MainViewModel(get(), get(), get())
    }

    viewModel {
        WebViewViewModel()
    }

    viewModel {
        ChapterViewModel(get(), get())
    }
}