package com.elta.android.data.features.reports.datasource

import android.net.Uri
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.data.features.common.network.ApiLocaleResolver
import com.elta.android.data.features.reports.api.ReportsApi
import com.elta.android.data.features.reports.dto.ReportNetworkRequest
import com.elta.android.domain.features.reports.model.ReportType
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
        reportType: ReportType,
        fileName: String
    ): Single<Uri> {
        val start = startDate.toStringWithFormat(DATE_PATTERN)
        val end = endDate.toStringWithFormat(DATE_PATTERN)

        return downloadReportV1(
            startDate = start,
            endDate = end,
            glucoseFormat = glucoseFormat.name,
            reportType = reportType,
            fileName = fileName
        )
    }


    private fun downloadReportV2(
        startDate: String,
        endDate: String,
        glucoseFormat: String,
        locale: String,
        timezoneOffset: String,
        reportType: ReportType,
        fileName: String
    ): Single<Uri> {
        val requestSingle = when (reportType) {
            ReportType.PDF -> reportsApi.downloadGlycemicProfileReport(
                reportPeriodStart = startDate,
                reportPeriodEnd = endDate,
                glucoseFormat = glucoseFormat,
                glucoseUnit = GLUCOSE_UNIT_MMOL_L,
                locale = locale,
                timezoneOffset = timezoneOffset
            )
            ReportType.XLSX -> reportsApi.downloadGlycemicProfileXlsxReport(
                reportPeriodStart = startDate,
                reportPeriodEnd = endDate,
                glucoseFormat = glucoseFormat,
                glucoseUnit = GLUCOSE_UNIT_MMOL_L,
                locale = locale,
                timezoneOffset = timezoneOffset
            )
        }
        return requestSingle.map { fileManager.saveReport(fileName, reportType.name.lowercase(), it) }
    }

    private fun downloadReportV1(
        startDate: String,
        endDate: String,
        glucoseFormat: String,
        reportType: ReportType,
        fileName: String
    ): Single<Uri> {
        val languageTag = ApiLocaleResolver.languageTag()
        return reportsApi.getReportToken(
            ReportNetworkRequest(
                startDate = startDate,
                endDate = endDate,
                glucoseFormat = glucoseFormat,
                languageTag = languageTag,
                locale = languageTag
            )
        ).flatMap { tokenDto ->
            reportsApi.downloadReport(tokenDto.token)
        }.map { fileManager.saveReport(fileName, reportType.name.lowercase(), it) }
    }

    companion object {
        private const val DATE_PATTERN = "yyyyMMdd"
        private const val GLUCOSE_UNIT_MMOL_L = "MMOL_L"
        private val FALLBACK_HTTP_CODES = setOf(404, 405)
    }
}
