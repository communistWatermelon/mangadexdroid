package com.melonhead.lib_chapter_cache

import android.content.Context
import com.melonhead.data_at_home.AtHomeService
import com.melonhead.lib_app_data.AppData
import com.melonhead.lib_database.chapter.ChapterEntity
import com.melonhead.lib_database.manga.MangaEntity
import com.melonhead.lib_logging.Clog
import com.melonhead.lib_networking.extensions.downloadFile
import io.ktor.client.HttpClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileFilter

sealed class CachingStatus {
    data object None: CachingStatus()
    data object StartedCacheOperation: CachingStatus()
    data object FinishedCacheOperation: CachingStatus()
}

interface ChapterCache {
    val cachingStatus: Flow<CachingStatus>
    fun getChapterFromCache(mangaId: String, chapterId: String): List<String>
    fun getChapterPageCountFromCache(mangaId: String, chapterId: String): Int?
    suspend fun cacheImagesForChapters(manga: List<MangaEntity>, chapters: List<ChapterEntity>)
    fun clearChapterFromCache(mangaId: String, chapterId: String)
    fun clearCacheForManga(mangaId: String)
}

internal class ChapterCacheImpl(
    private val appData: AppData,
    private val atHomeService: AtHomeService,
    private val appContext: Context,
    private val httpClient: HttpClient,
    private val externalScope: CoroutineScope,
) : ChapterCache {
    private val mutableCachingStatus = MutableStateFlow<CachingStatus>(CachingStatus.None)
    override val cachingStatus: Flow<CachingStatus>
        get() = mutableCachingStatus

    private val cacheWriteLock = Mutex()

    /* Wraps cache operation with status updates */
    private suspend fun cacheOperation(operation: suspend () -> Unit) {
        cacheWriteLock.withLock {
            mutableCachingStatus.value = CachingStatus.StartedCacheOperation
            operation()
            mutableCachingStatus.value = CachingStatus.FinishedCacheOperation
        }
    }

    private suspend fun getChapterData(chapterId: String): List<String>? {
        val chapterData = atHomeService.getChapterData(chapterId)
        return if (appData.useDataSaver) {
            chapterData?.pagesDataSaver()
        } else {
            chapterData?.pages()
        }
    }

    override fun getChapterFromCache(mangaId: String, chapterId: String): List<String> {
        val cacheDirectory = appContext.cacheDir
        val mangaDirectory = File(cacheDirectory, mangaId)
        val chapterDirectory = File(mangaDirectory, chapterId)
        val successFiles = chapterDirectory.listFiles(FileFilter { it.extension == "pages" }) ?: emptyArray()
        return if (chapterDirectory.exists() && successFiles.size == 1) {
            chapterDirectory.listFiles( FileFilter { it.extension != "pages" } ) ?.map { it.absolutePath } ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun getChapterPageCountFromCache(mangaId: String, chapterId: String): Int? {
        val cacheDirectory = appContext.cacheDir
        val mangaDirectory = File(cacheDirectory, mangaId)
        val chapterDirectory = File(mangaDirectory, chapterId)
        val successFiles = chapterDirectory.listFiles(FileFilter { it.extension == "pages" }) ?: return null
        if (successFiles.isEmpty()) return null
        return successFiles.first().nameWithoutExtension.toInt()
    }

    override suspend fun cacheImagesForChapters(manga: List<MangaEntity>, chapters: List<ChapterEntity>) {
        cacheOperation {
            val cacheDirectory = appContext.cacheDir
            for (chapter in chapters) {
                val mangaForChapter = manga.find { it.id == chapter.mangaId } ?: continue
                if (mangaForChapter.useWebview) continue
                val mangaDirectory = File(cacheDirectory, mangaForChapter.id)

                if (!mangaDirectory.exists()) {
                    mangaDirectory.mkdir()
                }

                val chapterDirectory = File(mangaDirectory, chapter.id)
                if (!chapterDirectory.exists()) {
                    chapterDirectory.mkdir()
                }

                if (chapterDirectory.listFiles(FileFilter { it.extension == "pages" })?.isNotEmpty() == true) continue
                val chapterData = getChapterData(chapter.id)
                if (chapterData.isNullOrEmpty()) continue

                Clog.i("Caching images for manga ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle}")

                val oldFiles = chapterDirectory.listFiles() ?: arrayOf()
                if (oldFiles.none { it.extension == "pages" } && oldFiles.count() != chapterData.count()) {
                    Clog.w("Found bad file count for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle}")
                    for (file in chapterDirectory.listFiles()!!) {
                        file.delete()
                    }
                }

                val chapterTitle = chapter.chapterTitle ?: chapter.chapter
                Clog.i("Downloading images to cache for ${mangaForChapter.chosenTitle} chapter $chapterTitle")
                val jobsList = mutableListOf<Deferred<Boolean>>()
                withContext(Dispatchers.IO) {
                    for ((i, page) in chapterData.withIndex()) {
                        Clog.i("Downloading page $i for ${mangaForChapter.chosenTitle} chapter $chapterTitle - $page")
                        val fileExtension = page.substringAfterLast(".")
                        val pageFile = File(chapterDirectory, "$i.$fileExtension")
                        if (pageFile.exists()) {
                            pageFile.delete()
                        } else {
                            pageFile.createNewFile()
                        }
                        val downloadJob = async {
                            try {
                                val result = httpClient.downloadFile(pageFile, page)
                                if (result) {
                                    Clog.i("Finished downloading page $i for ${mangaForChapter.chosenTitle} chapter $chapterTitle - $page")
                                    true
                                } else {
                                    pageFile.delete()
                                    Clog.w("Failed to download page $i for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle} - $page")
                                    false
                                }
                            } catch (e: Exception) {
                                pageFile.delete()
                                Clog.w("Error downloading page $i for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle} - $page")
                                Clog.e("Error downloading page", e)
                                false
                            }
                        }
                        jobsList.add(downloadJob)
                    }
                }
                if (jobsList.awaitAll().any { false } || jobsList.size != chapterData.count()) {
                    chapterDirectory.deleteRecursively()
                    Clog.w("Failed to download images to cache for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle}")
                    continue
                }

                val newFiles = chapterDirectory.listFiles() ?: continue
                if (newFiles.count() == chapterData.count()) {
                    val successFile = File(chapterDirectory, "${chapterData.count()}.pages")
                    successFile.createNewFile()
                    Clog.i("Finished downloading images to cache for ${mangaForChapter.chosenTitle} chapter $chapterTitle")
                }
            }
        }
    }

    override fun clearChapterFromCache(mangaId: String, chapterId: String) {
        externalScope.launch {
            cacheOperation {
                try {
                    Clog.i("Clearing cache for manga $mangaId chapter $chapterId")
                    val cacheDirectory = appContext.cacheDir
                    val mangaDirectory = File(cacheDirectory, mangaId)
                    val chapterDirectory = File(mangaDirectory, chapterId)
                    if (chapterDirectory.exists()) {
                        val result = chapterDirectory.deleteRecursively()
                        Clog.i("Cleared cache for manga $mangaId chapter $chapterId - $result")
                    }
                } catch (e: Exception) {
                    Clog.w("Error clearing cache for manga $mangaId chapter $chapterId")
                    Clog.e("Error clearing cache for manga", e)
                }
            }
        }
    }

    override fun clearCacheForManga(mangaId: String) {
        externalScope.launch  {
            cacheOperation {
                try {
                    Clog.i("Clearing cache for manga $mangaId")
                    val cacheDirectory = appContext.cacheDir
                    val mangaDirectory = File(cacheDirectory, mangaId)
                    if (mangaDirectory.exists()) {
                        val result = mangaDirectory.deleteRecursively()
                        Clog.i("Cleared cache for manga $mangaId - $result")
                    }
                } catch (e: Exception) {
                    Clog.w("Error clearing cache for manga $mangaId")
                    Clog.e("Error clearing cache for manga", e)
                }
            }
        }
    }
}
