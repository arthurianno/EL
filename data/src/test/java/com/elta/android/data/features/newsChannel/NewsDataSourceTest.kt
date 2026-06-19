package com.elta.android.data.features.newsChannel

import android.content.Context
import android.content.SharedPreferences
import com.elta.android.data.features.common.network.ApiCountryCodeResolver
import com.elta.android.data.features.newsChannel.datasource.AttachmentMetaDataDto
import com.elta.android.data.features.newsChannel.datasource.NewsApi
import com.elta.android.data.features.newsChannel.datasource.NewsDataSource
import com.elta.android.data.features.newsChannel.datasource.NewsDto
import com.elta.android.data.features.newsChannel.datasource.NewsListResponseDto
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class NewsDataSourceTest {

    private val api = FakeNewsApi()
    private val context: Context = mock()
    private val sharedPrefs: SharedPreferences = mock()
    private val resolver = ApiCountryCodeResolver(context)
    private val baseUrl = "https://example.com"
    private val dataSource = NewsDataSource(api, resolver, baseUrl)

    init {
        whenever(context.getSharedPreferences("language_preference", 0)).thenReturn(sharedPrefs)
        whenever(sharedPrefs.getString("selected_region", null)).thenReturn("RU")
    }

    @Test
    fun `fetchNewsList maps new API v2 fields correctly`() = runBlocking {
        // Given
        val newsId = UUID.randomUUID()
        val mockDto = NewsDto(
            id = newsId,
            title = "Test Title",
            content = "Test Content",
            createdDateTime = "2026-06-18T12:00:00Z",
            modifiedDateTime = null,
            attachmentMetaData = AttachmentMetaDataDto(
                fileName = "document.pdf",
                fileSize = 1024L,
                fileUri = "/api/news/v2/attachments/doc123"
            ),
            imageUri = "/api/news/v2/images/img123",
            orderNumber = 1L,
            state = "ACTIVE"
        )
        val mockResponse = NewsListResponseDto(
            content = listOf(mockDto),
            hasNextPage = false,
            endCursor = null
        )

        api.responseDto = mockResponse

        // When
        val result = dataSource.fetchNewsList(null, 10, "DESC")

        // Then
        assertEquals(1, result.news.size)
        val domainNews = result.news.first()
        assertEquals(newsId, domainNews.id)
        assertEquals("Test Title", domainNews.title)
        assertEquals("Test Content", domainNews.content)

        // Verify attachment mapping
        assertNotNull(domainNews.file)
        assertEquals("document.pdf", domainNews.file!!.name)
        assertEquals(1024L, domainNews.file!!.size)
        assertEquals("https://example.com/api/news/v2/attachments/doc123", domainNews.file!!.url)

        // Verify image mapping
        assertNotNull(domainNews.image)
        assertEquals("https://example.com/api/news/v2/images/img123", domainNews.image!!.url)
        assertNull(domainNews.image!!.data)
    }

    @Test
    fun `fetchNewsList falls back to legacy fields when v2 fields are absent`() = runBlocking {
        // Given
        val newsId = UUID.randomUUID()
        val mockDto = NewsDto(
            id = newsId,
            title = "Legacy Title",
            content = "Legacy Content",
            createdDateTime = "2026-06-18T12:00:00Z",
            modifiedDateTime = null,
            attachmentMetaData = null,
            imageUri = null,
            orderNumber = 2L,
            state = "ACTIVE",
            fileName = "legacy_doc.pdf",
            fileSize = 512L,
            imageData = "base64encodedimageString"
        )
        val mockResponse = NewsListResponseDto(
            content = listOf(mockDto),
            hasNextPage = false,
            endCursor = null
        )

        api.responseDto = mockResponse

        // When
        val result = dataSource.fetchNewsList(null, 10, "DESC")

        // Then
        assertEquals(1, result.news.size)
        val domainNews = result.news.first()
        assertEquals(newsId, domainNews.id)

        // Verify fallback attachment mapping
        assertNotNull(domainNews.file)
        assertEquals("legacy_doc.pdf", domainNews.file!!.name)
        assertEquals(512L, domainNews.file!!.size)
        assertEquals("https://example.com/api/news/$newsId/file", domainNews.file!!.url)

        // Verify fallback image mapping
        assertNotNull(domainNews.image)
        assertNull(domainNews.image!!.url)
        assertEquals("base64encodedimageString", domainNews.image!!.data)
    }

    private class FakeNewsApi : NewsApi {
        var responseDto: NewsListResponseDto? = null

        override suspend fun getNewsList(
            cursor: Long?,
            limit: Int?,
            direction: String?,
            languageTag: String,
            platform: String,
            appVersion: String,
            countryCode: String
        ): NewsListResponseDto {
            return responseDto ?: throw IllegalStateException("Response not set in FakeNewsApi")
        }

        override suspend fun getNewsById(id: String): NewsDto = throw NotImplementedError()
        override suspend fun getNewsFile(id: String): ByteArray = throw NotImplementedError()
    }
}
