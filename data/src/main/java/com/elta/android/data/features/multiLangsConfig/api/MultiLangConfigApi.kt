package com.elta.android.data.features.multiLangsConfig.api

import com.elta.android.data.features.multiLangsConfig.dto.ScreenResponseDto
import retrofit2.Response
import retrofit2.http.GET


interface MultiLangConfigApi {
    @GET("https://test.vdiabete.com/api/config/screens/by-slugs?langs=ru&langs=en&langs=kz")
    suspend fun getAllScreensBySlugs(
    ): Response<ScreenResponseDto>
}