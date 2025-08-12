package com.elta.android.domain.features.newsChannel.interactor


import android.util.Log
import com.elta.android.domain.features.newsChannel.model.NewsListResponse
import com.elta.android.domain.features.newsChannel.repository.NewsRepository
import javax.inject.Inject

class LoadMessagesNewsUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(
        cursor: Long? = null,
        limit: Int = 10,
        direction: String? = "DESC"
    ): NewsListResponse {
        val response = repository.getNewsList(cursor, limit, direction)
        Log.e("LoadMessagesNewsUseCase", "Cursor $cursor, Limit $limit, Direction $direction, News: ${response.news.map { it.id }}")
        return response
    }
}