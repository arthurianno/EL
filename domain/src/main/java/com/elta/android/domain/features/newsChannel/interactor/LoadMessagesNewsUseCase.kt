package com.elta.android.domain.features.newsChannel.interactor

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
    ): NewsListResponse = repository.getNewsList(cursor, limit, direction)
}
