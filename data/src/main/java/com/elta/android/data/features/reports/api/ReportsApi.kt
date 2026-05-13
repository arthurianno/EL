package com.elta.android.data.features.reports.api

import com.elta.android.data.features.reports.dto.ReportNetworkRequest
import com.elta.android.data.features.reports.dto.TokenDto
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ReportsApi {

    @POST("api/reports/v1/analytics/tokens")
    fun getReportToken(@Body report: ReportNetworkRequest): Single<TokenDto>

    @GET("api/reports/v1/analytics/tokens/observables/{observable_id}")
    fun getObservableReportToken(
        @Path("observable_id") observableId: String,
        @Query("startPeriod") startPeriod: String,
        @Query("endPeriod") endPeriod: String,
        @Query("glucoseFormat") glucoseFormat: String,
        @Query("languageTag") languageTag: String
    ): Single<TokenDto>

    @GET("api/reports/v2/glycemic-profile")
    fun downloadGlycemicProfileReport(
        @Query("reportPeriodStart") reportPeriodStart: String,
        @Query("reportPeriodEnd") reportPeriodEnd: String,
        @Query("glucoseFormat") glucoseFormat: String,
        @Query("glucoseUnit") glucoseUnit: String,
        @Query("locale") locale: String,
        @Query("timezoneOffset") timezoneOffset: String
    ): Single<ResponseBody>

    @GET("api/reports/v1/analytics")
    fun downloadReport(@Query("token") token: String): Single<ResponseBody>
}
