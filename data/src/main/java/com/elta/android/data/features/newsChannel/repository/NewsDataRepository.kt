package com.elta.android.data.features.newsChannel.repository

import com.elta.android.data.features.newsChannel.datasource.NewsDataSource
import com.elta.android.domain.features.newsChannel.model.News
import com.elta.android.domain.features.newsChannel.model.NewsListResponse
import com.elta.android.domain.features.newsChannel.repository.NewsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsDataRepository @Inject constructor(
    private val dataSource: NewsDataSource,
    override val dispatcher: CoroutineDispatcher
) : NewsRepository {

    private val _newsList = MutableStateFlow(NewsListResponse(emptyList(), false, null))
    override val newsList: Flow<NewsListResponse> = _newsList.asStateFlow()

    override suspend fun getNewsList(cursor: Long?, limit: Int?, direction: String?): NewsListResponse {
        return withContext(dispatcher) {
            try {
                val response = dataSource.fetchNewsList(cursor, limit, direction)
                Timber.d("getNewsList: fetched ${response.news.size} news, cursor=$cursor, limit=$direction")
                _newsList.emit(response)
                response
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch news list: ${e.message}")
                throw e
            }
        }
    }

    override suspend fun getNewsById(id: UUID): News {
        return withContext(dispatcher) {
            try {
                val news = dataSource.fetchNewsById(id)
                Timber.d("Fetched news with ID: $id")
                news
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch news by ID: $id, ${e.message}")
                throw IllegalStateException("Failed to fetch news with ID: $id")
            }
        }
    }

    override suspend fun saveNewsToCache(news: List<News>) {
        // No-op as cache is removed
    }

    override suspend fun clearCache() {
        // No-op as cache is removed
    }

    override suspend fun getCachedNews(): List<News> {
        return emptyList() // No cache available
    }
}