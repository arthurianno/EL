package com.elta.android.domain.features.newsChannel.model

data class NewsImage(
    val data: String?, // Base64 или URL изображения
    val url: String? // URL для загрузки изображения (если нужен)
)