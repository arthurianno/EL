package com.elta.android.data.features.newsChannel.datasource
import com.elta.android.data.BuildConfig
import com.elta.android.data.core.qualifires.ServerUrl
import com.elta.android.data.features.common.network.ApiCountryCodeResolver
import com.elta.android.data.features.common.network.ApiLocaleResolver
import com.elta.android.domain.features.newsChannel.model.News
import com.elta.android.domain.features.newsChannel.model.NewsFile
import com.elta.android.domain.features.newsChannel.model.NewsImage
import com.elta.android.domain.features.newsChannel.model.NewsListResponse
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import java.util.UUID
import javax.inject.Inject
class NewsDataSource @Inject constructor(
    private val api: NewsApi,
    private val countryCodeResolver: ApiCountryCodeResolver,
    @ServerUrl private val baseUrl: String
) {
    suspend fun fetchNewsList(cursor: Long?, limit: Int?, direction: String?): NewsListResponse {
        try {
            val params = buildRequestParams(cursor, limit, direction)
            val response = api.getNewsList(
                cursor = params.cursor,
                limit = params.limit,
                direction = params.direction,
                languageTag = params.languageTag,
                platform = params.platform,
                appVersion = params.appVersion,
                countryCode = params.countryCode
            )
            val domainResponse = response.toDomain(baseUrl)  // Передаём baseUrl
            return domainResponse
        } catch (e: Exception) {
            throw e
        }
    }
    suspend fun fetchNewsById(id: UUID): News {
        val response = api.getNewsById(id.toString())
        return response.toDomain(baseUrl)  // Передаём baseUrl
    }
    suspend fun fetchNewsFile(id: UUID): ByteArray {
        return api.getNewsFile(id.toString())
    }

    fun buildRequestParams(cursor: Long?, limit: Int?, direction: String?): NewsRequestParams =
        NewsRequestParams(
            cursor = cursor,
            limit = limit?.coerceIn(MIN_LIMIT, MAX_LIMIT),
            direction = direction,
            languageTag = ApiLocaleResolver.languageTag(),
            platform = MOBILE_PLATFORM_ANDROID,
            appVersion = BuildConfig.VERSION_NAME,
            countryCode = countryCodeResolver.countryCode()
        )

    private companion object {
        const val MOBILE_PLATFORM_ANDROID = "android"
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 100
    }
}
private fun NewsListResponseDto.toDomain(baseUrl: String): NewsListResponse {
    return NewsListResponse(
        news = content?.filter { it.state.equals("ACTIVE", ignoreCase = true) }?.map { it.toDomain(baseUrl) } ?: emptyList(),
        hasNextPage = hasNextPage,
        endCursor = endCursor
    )
}
private fun NewsDto.toDomain(baseUrl: String): News {
    return News(
        id = id,
        title = title,
        content = content,
        createdDateTime = createdDateTime.toEpochMilli(),
        modifiedDateTime = modifiedDateTime?.toEpochMilli(),
        file = fileName?.let {
            NewsFile(
                name = it,
                url = "$baseUrl/api/news/$id/file",
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
        0L // Или выбросить исключение, если это критично
    }
}
