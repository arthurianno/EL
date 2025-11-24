package com.elta.android.domain.features.newsChannel.model

data class NewsFile(
    val name: String,
    val size: Long, // Размер файла в байтах
    val url: String? // URL для загрузки файла (если нужен)
)