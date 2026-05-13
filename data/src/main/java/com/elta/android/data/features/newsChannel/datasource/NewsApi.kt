package com.elta.android.data.features.newsChannel.datasource

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApi {
    @GET("api/news")
    suspend fun getNewsList(
        @Query("cursor") cursor: Long? = null,
        @Query("limit") limit: Int? = 10,
        @Query("direction") direction: String? = "DESC",
        @Query("languageTag") languageTag: String,
        @Query("platform") platform: String,
        @Query("appVersion") appVersion: String,
        @Query("countryCode") countryCode: String
    ): NewsListResponseDto

    @GET("api/news{id}")
    suspend fun getNewsById(
        @Path("id") id: String
    ): NewsDto

    @GET("api/news{id}/file")
    suspend fun getNewsFile(
        @Path("id") id: String
    ): ByteArray
}
