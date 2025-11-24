package com.elta.android.data.features.multiLang.api

import com.elta.android.data.features.multiLang.models.ScreenConfigResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ConfigApi {

    @GET("/config/screens/by-slugs")
    fun getScreenConfigs(
        @Query("slugs") slugs: List<String>,
        @Query("langs") langs: List<String>? = null
    ): Single<List<ScreenConfigResponse>>
}