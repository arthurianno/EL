package com.elta.android.data.features.newsChannel.datasource

import android.util.Log
import com.elta.android.domain.features.newsChannel.model.News
import com.elta.android.domain.features.newsChannel.model.NewsFile
import com.elta.android.domain.features.newsChannel.model.NewsImage
import com.elta.android.domain.features.newsChannel.model.NewsListResponse
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import java.util.UUID
import javax.inject.Inject

class NewsDataSource @Inject constructor(
    private val api: NewsApi
) {
    suspend fun fetchNewsList(cursor: Long?, limit: Int?, direction: String?): NewsListResponse {
        try {
            Log.d("NewsDataSource", "Fetching news with cursor=$cursor, limit=$limit, direction=$direction")
            val response = api.getNewsList(cursor, limit, direction)
            Log.e("RESPONSE GET", "Raw response: $response")
            val domainResponse = response.toDomain()
            Log.e("RESPONSE GET", "Domain response: $domainResponse")
            return domainResponse
        } catch (e: Exception) {
            Log.e("RESPONSE GET", "Error fetching news: ${e.message}", e)
            throw e
        }
    }

    suspend fun fetchNewsById(id: UUID): News {
        val response = api.getNewsById(id.toString())
        return response.toDomain()
    }

    suspend fun fetchNewsFile(id: UUID): ByteArray {
        return api.getNewsFile(id.toString())
    }
}

private fun NewsListResponseDto.toDomain(): NewsListResponse {
    return NewsListResponse(
        news = content?.filter { it.state.equals("ACTIVE", ignoreCase = true) }?.map { it.toDomain() } ?: emptyList(),
        hasNextPage = hasNextPage,
        endCursor = endCursor
    )
}

private fun NewsDto.toDomain(): News {
    return News(
        id = id,
        title = title,
        content = content,
        createdDateTime = createdDateTime.toEpochMilli(),
        modifiedDateTime = modifiedDateTime?.toEpochMilli(),
        file = fileName?.let {
            NewsFile(
                name = it,
                url = "https://dev.vdiabete.com/api/news/$id/file",
                size = fileSize ?: 0
            )
        },
        image = imageData?.let { NewsImage(url = null, data = it) },
        orderNumber = orderNumber ?: 0L,
        state = state.toString()
    )
}

private fun String?.toEpochMilli(): Long {
    if (this.isNullOrBlank()) {
        Log.e("NewsDataSource", "Timestamp is null or blank")
        return 0L // Или выбросить исключение, если это критично
    }

    return try {
        // Проверяем, является ли строка числом (Unix timestamp)
        if (this.matches(Regex("\\d+\\.\\d+"))) {
            val secondsPart = substringBefore(".").toLong()
            val nanosPart = substringAfter(".", "0").padEnd(9, '0').toLong() / 1_000_000 // Наносекунды в миллисекунды
            secondsPart * 1000 + nanosPart
        } else {
            // Предполагаем формат ISO-8601
            LocalDateTime.parse(this).toInstant(ZoneOffset.UTC).toEpochMilli()
        }
    } catch (e: Exception) {
        Log.e("NewsDataSource", "Error parsing timestamp: $this", e)
        0L // Или выбросить исключение, если это критично
    }
}