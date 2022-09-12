package com.elta.android.data.features.reports.api

import com.elta.android.data.features.reports.dto.TokenDto
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportsApi {

    @GET("api/reports/v1/analytics/tokens")
    fun getReportToken(@Query("startPeriod") startDate: String, @Query("endPeriod") endDate: String): Single<TokenDto>

    @GET("api/reports/v1/analytics")
    fun downloadReport(@Query("token") token: String): Single<ResponseBody>
}
