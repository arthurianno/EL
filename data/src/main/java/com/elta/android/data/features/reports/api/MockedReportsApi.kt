package com.elta.android.data.features.reports.api

import android.content.Context
import com.elta.android.data.R
import com.elta.android.data.features.reports.dto.ReportNetworkRequest
import com.elta.android.data.features.reports.dto.TokenDto
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody

class MockedReportsApi(
    private val context: Context
) : ReportsApi {

    override fun getReportToken(report: ReportNetworkRequest): Single<TokenDto> =
        Single.just(TokenDto("Test-token-bro"))

    override fun getObservableReportToken(
        observableId: String,
        startPeriod: String,
        endPeriod: String,
        glucoseFormat: String,
        languageTag: String
    ): Single<TokenDto> = Single.just(TokenDto("Test-observable-token-bro"))

    override fun downloadGlycemicProfileReport(
        reportPeriodStart: String,
        reportPeriodEnd: String,
        glucoseFormat: String,
        glucoseUnit: String,
        locale: String,
        timezoneOffset: String
    ): Single<ResponseBody> =
        loadReport()

    override fun downloadReport(token: String): Single<ResponseBody> =
        loadReport()

    private fun loadReport(): Single<ResponseBody> =
        Single.fromCallable {
            val stream = context.resources.openRawResource(R.raw.test_report)
            stream.readBytes().toResponseBody("application/octet-stream".toMediaTypeOrNull())
        }
}
