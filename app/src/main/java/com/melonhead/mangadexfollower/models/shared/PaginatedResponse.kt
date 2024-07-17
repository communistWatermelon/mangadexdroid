package com.melonhead.mangadexfollower.models.shared

import com.melonhead.lib_logging.Clog
import io.ktor.client.call.*
import io.ktor.client.statement.*

@kotlinx.serialization.Serializable
data class PaginatedResponse<T>(val limit: Int, val offset: Int, val total: Int, val data: List<T>)

suspend inline fun <reified T> handlePagination(
    expectedCount: Int = Int.MAX_VALUE,
    fetchAll: Boolean = true,
    request: (offset: Int) -> HttpResponse?
): List<T> {
    var total = expectedCount
    val allItems = mutableSetOf<T>()
    while (allItems.count() < total) {
        val result = request(allItems.count()) ?: continue
        val items = try {
            val response = result.body<PaginatedResponse<T>>()
            if (fetchAll) total = response.total
            response.data
        } catch (e: Exception) {
            Clog.e("handlePagination: ${result.bodyAsText()}", e)
            break
        }
        allItems.addAll(items)
    }
    return allItems.toList()
}
