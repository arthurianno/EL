package com.elta.android.domain.features.newsChannel.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.newsChannel.model.News
import com.elta.android.domain.features.newsChannel.model.NewsListResponse
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface NewsRepository : BaseRepository {
    suspend fun getNewsList(cursor: Long?, limit: Int?, direction: String?): NewsListResponse
    suspend fun getNewsById(id: UUID): News
    suspend fun getCachedNews(): List<News>
    suspend fun saveNewsToCache(news: List<News>)
    suspend fun clearCache()
    val newsList: Flow<NewsListResponse>
}