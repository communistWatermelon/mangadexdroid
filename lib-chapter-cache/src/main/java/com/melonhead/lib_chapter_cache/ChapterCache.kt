package com.melonhead.lib_chapter_cache

import android.content.Context
import com.melonhead.lib_app_data.AppData
import com.melonhead.data_at_home.AtHomeService
import com.melonhead.lib_database.chapter.ChapterEntity
import com.melonhead.lib_database.manga.MangaEntity
import com.melonhead.lib_logging.Clog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.net.URL

interface ChapterCache {
    fun cacheImagesForChapters(manga: List<MangaEntity>, chapters: List<ChapterEntity>)
    fun getChapterFromCache(mangaId: String, chapterId: String): List<String>
    fun clearChapterFromCache(mangaId: String, chapterId: String)
    fun getChapterPageCountFromCache(mangaId: String, chapterId: String): Int?
}

internal class ChapterCacheImpl(
    private val appData: AppData,
    private val atHomeService: AtHomeService,
    private val externalScope: CoroutineScope,
    private val appContext: Context,
) : ChapterCache {

    private suspend fun getChapterData(chapterId: String): List<String>? {
        val chapterData = atHomeService.getChapterData(chapterId)
        return if (appData.useDataSaver) {
            chapterData?.pagesDataSaver()
        } else {
            chapterData?.pages()
        }
    }

    override fun cacheImagesForChapters(manga: List<MangaEntity>, chapters: List<ChapterEntity>) {
        Clog.d("Caching images for ${chapters.count()} chapters")
        for (chapter in chapters) {
            val mangaForChapter = manga.find { it.id == chapter.mangaId } ?: continue
            if (mangaForChapter.useWebview) continue
            externalScope.launch(Dispatchers.IO) {
                Clog.d("Caching images for manga ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle}")
                val cacheDirectory = appContext.cacheDir
                val mangaDirectory = File(cacheDirectory, mangaForChapter.id)

                val chapterData = getChapterData(chapter.id)
                if (chapterData.isNullOrEmpty()) return@launch

                if (!mangaDirectory.exists()) {
                    mangaDirectory.mkdir()
                }

                val chapterDirectory = File(mangaDirectory, chapter.id)
                if (!chapterDirectory.exists()) {
                    chapterDirectory.mkdir()
                }

                if (chapterDirectory.listFiles(FileFilter { it.extension == "pages" })?.isNotEmpty() == true) return@launch

                val oldFiles = chapterDirectory.listFiles() ?: arrayOf()
                if (oldFiles.none { it.extension == "pages" } && oldFiles.count() != chapterData.count()) {
                    for (file in chapterDirectory.listFiles()!!) {
                        file.delete()
                    }
                }

                Clog.d("Downloading images to cache for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle}")
                for ((i, page) in chapterData.withIndex()) {
                    Clog.d("Downloading page $i for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle} - $page")
                    val fileExtension = page.substringAfterLast(".")
                    val pageFile = File(chapterDirectory, "$i.$fileExtension")
                    pageFile.createNewFile()

                    try {
                        val oStream = FileOutputStream(pageFile)
                        val inputStream = URL(page).openStream()
                        copy(inputStream, oStream)
                        oStream.flush()
                        inputStream.close()
                        oStream.close()
                    } catch (e: Exception) {
                        Clog.i("Error downloading page $i for ${mangaForChapter.chosenTitle} chapter ${chapter.chapterTitle} - $page")
                        Clog.e("Error downloading page", e)
                    }
                }

                val newFiles = chapterDirectory.listFiles() ?: return@launch
                if (newFiles.count() == chapterData.count()) {
                    val successFile = File(chapterDirectory, "${chapterData.count()}.pages")
                    successFile.createNewFile()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
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

    override fun clearChapterFromCache(mangaId: String, chapterId: String) {
        try {
            Clog.d("Clearing cache for manga $mangaId chapter $chapterId")
            val cacheDirectory = appContext.cacheDir
            val mangaDirectory = File(cacheDirectory, mangaId)
            val chapterDirectory = File(mangaDirectory, chapterId)
            if (chapterDirectory.exists()) {
                val result = chapterDirectory.deleteRecursively()
                Clog.d("Cleared cache for manga $mangaId chapter $chapterId - $result")
            }
        } catch (e: Exception) {
            Clog.i("Error clearing cache for manga $mangaId chapter $chapterId")
            Clog.e("Error clearing cache for manga", e)
        }
    }
}
