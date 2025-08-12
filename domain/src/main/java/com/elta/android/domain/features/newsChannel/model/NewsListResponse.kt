package com.elta.android.domain.features.newsChannel.model

data class NewsListResponse(
    val news: List<News>,
    val hasNextPage: Boolean, // Новое поле
    val endCursor: Long? // Новое поле
)