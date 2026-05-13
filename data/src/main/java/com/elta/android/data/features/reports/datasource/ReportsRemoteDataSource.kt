package com.elta.android.data.features.reports.datasource

import android.net.Uri
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.data.features.common.network.ApiLocaleResolver
import com.elta.android.data.features.reports.api.ReportsApi
import com.elta.android.data.features.reports.dto.ReportNetworkRequest
import com.elta.android.domain.features.user.model.GlucoseFormat
import io.reactivex.Single
import org.threeten.bp.LocalDate
import retrofit2.HttpException
import javax.inject.Inject

class ReportsRemoteDataSource @Inject constructor(
    private val reportsApi: ReportsApi,
    private val fileManager: ReportFileManager
) : ReportsDataSource {

    override fun downloadReport(
        startDate: LocalDate,
        endDate: LocalDate,
        glucoseFormat: GlucoseFormat,
        fileName: String
    ): Single<Uri> {
        val start = startDate.toStringWithFormat(DATE_PATTERN)
        val end = endDate.toStringWithFormat(DATE_PATTERN)
        val locale = ApiLocaleResolver.reportLocale()
        val timezoneOffset = ApiLocaleResolver.timezoneOffset()

        return downloadReportV2(
            startDate = start,
            endDate = end,
            glucoseFormat = glucoseFormat.name,
            locale = locale,
            timezoneOffset = timezoneOffset,
            fileName = fileName
        ).onErrorResumeNext { error ->
            if (error is HttpException && error.code() in FALLBACK_HTTP_CODES) {
                downloadReportV1(
                    startDate = start,
                    endDate = end,
                    glucoseFormat = glucoseFormat.name,
                    fileName = fileName
                )
            } else {
                Single.error(error)
            }
        }
    }

    private fun downloadReportV2(
        startDate: String,
        endDate: String,
        glucoseFormat: String,
        locale: String,
        timezoneOffset: String,
        fileName: String
    ): Single<Uri> =
        reportsApi.downloadGlycemicProfileReport(
            reportPeriodStart = startDate,
            reportPeriodEnd = endDate,
            glucoseFormat = glucoseFormat,
            glucoseUnit = GLUCOSE_UNIT_MMOL_L,
            locale = locale,
            timezoneOffset = timezoneOffset
        ).map { fileManager.saveReport(fileName, it) }

    private fun downloadReportV1(
        startDate: String,
        endDate: String,
        glucoseFormat: String,
        fileName: String
    ): Single<Uri> {
        val languageTag = ApiLocaleResolver.languageTag()
        return reportsApi.getReportToken(
            ReportNetworkRequest(
                startDate = startDate,
                endDate = endDate,
                glucoseFormat = glucoseFormat,
                languageTag = languageTag
            )
        ).flatMap { tokenDto ->
            reportsApi.downloadReport(tokenDto.token)
        }.map { fileManager.saveReport(fileName, it) }
    }

    companion object {
        private const val DATE_PATTERN = "yyyyMMdd"
        private const val GLUCOSE_UNIT_MMOL_L = "MMOL_L"
        private val FALLBACK_HTTP_CODES = setOf(404, 405)
    }
}
