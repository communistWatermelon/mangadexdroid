package com.melonhead.lib_chapter_cache

import android.app.DownloadManager
import android.content.Context
import androidx.core.content.getSystemService
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
import java.io.File
import java.io.FileFilter

sealed class CachingStatus {
    data object None: CachingStatus()
    data object StartedCaching: CachingStatus()
    data object FinishedCaching: CachingStatus()
}

interface ChapterCache {
    suspend fun cacheImagesForChapters(manga: List<MangaEntity>, chapters: List<ChapterEntity>)
    fun getChapterFromCache(mangaId: String, chapterId: String): List<String>
    fun clearChapterFromCache(mangaId: String, chapterId: String)
    fun getChapterPageCountFromCache(mangaId: String, chapterId: String): Int?
    val cachingStatus: Flow<CachingStatus>
}

internal class ChapterCacheImpl(
    private val appData: AppData,
    private val atHomeService: AtHomeService,
    private val externalScope: CoroutineScope,
    private val appContext: Context,
    private val httpClient: HttpClient,
) : ChapterCache {
    private val mutableCachingStatus = MutableStateFlow<CachingStatus>(CachingStatus.None)
    override val cachingStatus: Flow<CachingStatus>
        get() = mutableCachingStatus

    private val downloadManager = appContext.getSystemService<DownloadManager>()!!

    private suspend fun getChapterData(chapterId: String): List<String>? {
        val chapterData = atHomeService.getChapterData(chapterId)
        return if (appData.useDataSaver) {
            chapterData?.pagesDataSaver()
        } else {
            chapterData?.pages()
        }
    }

    override suspend fun cacheImagesForChapters(manga: List<MangaEntity>, chapters: List<ChapterEntity>) {
        mutableCachingStatus.value = CachingStatus.StartedCaching
        Clog.i("Caching images for ${chapters.count()} chapters")
        for (chapter in chapters) {
            val mangaForChapter = manga.find { it.id == chapter.mangaId } ?: continue
            if (mangaForChapter.useWebview) continue
            Clog.i("Caching images for manga ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle}")
            val cacheDirectory = appContext.cacheDir
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
                    val downloadJob = async {
                        Clog.i("Downloading page $i for ${mangaForChapter.chosenTitle} chapter $chapterTitle - $page")
                        val fileExtension = page.substringAfterLast(".")
                        val pageFile = File(chapterDirectory, "$i.$fileExtension")
                        pageFile.createNewFile()

                        try {
                            val result = httpClient.downloadFile(pageFile, page)
                            if (!result) {
                                pageFile.delete()
                                Clog.w("Error downloading page $i for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle} - $page")
                                return@async false
                            }
                            Clog.i("Finished downloading page $i for ${mangaForChapter.chosenTitle} chapter $chapterTitle - $page")
                            true
                        } catch (e: Exception) {
                            Clog.w("Error downloading page $i for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle} - $page")
                            Clog.e("Error downloading page", e)
                            false
                        }
                    }
                    jobsList.add(downloadJob)
                }
            }
            if (jobsList.awaitAll().any { false }) {
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
        mutableCachingStatus.value = CachingStatus.FinishedCaching
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

    override fun clearChapterFromCache(mangaId: String, chapterId: String) {
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
