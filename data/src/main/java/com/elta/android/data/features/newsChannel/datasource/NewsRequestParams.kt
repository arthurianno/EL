package com.elta.android.data.features.newsChannel.datasource

data class NewsRequestParams(
    val cursor: Long?,
    val limit: Int?,
    val direction: String?,
    val languageTag: String,
    val platform: String,
    val appVersion: String,
    val countryCode: String
)
