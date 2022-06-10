package com.elta.android.data.features.reports.api

import android.content.Context
import com.elta.android.data.R
import com.elta.android.data.features.reports.dto.TokenDto
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody

class MockedReportsApi(
    private val context: Context
) : ReportsApi {

    override fun getReportToken(startDate: String, endDate: String): Single<TokenDto> =
        Single.just(TokenDto("Test-token-bro"))

    override fun downloadReport(token: String): Single<ResponseBody> =
        Single.fromCallable {
            val stream = context.resources.openRawResource(R.raw.test_report)
            ResponseBody.create(MediaType.parse("application/octet-stream"), stream.readBytes())
        }
}
