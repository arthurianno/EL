package com.elta.android.data.features.newsChannel.datasource
import com.google.gson.annotations.SerializedName // или import com.squareup.moshi.Json для Moshi
import java.util.UUID
// DTO для ответа API
data class NewsListResponseDto(
    @SerializedName("content") val content: List<NewsDto>?,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("endCursor") val endCursor: Long?
)
// DTO для отдельной новости
data class NewsDto(
    @SerializedName("id") val id: UUID,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("createdDateTime") val createdDateTime: String,
    @SerializedName("modifiedDateTime") val modifiedDateTime: String?,
    @SerializedName("fileName") val fileName: String?,
    @SerializedName("fileSize") val fileSize: Long?,
    @SerializedName("imageData") val imageData: String?,
    @SerializedName("orderNumber") val orderNumber: Long?,
    @SerializedName("state") val state: String?
)