package com.elta.android.data.features.reports.datasource

import android.net.Uri
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.data.features.reports.api.ReportsApi
import com.elta.android.data.features.reports.dto.TokenDto
import io.reactivex.Single
import org.threeten.bp.LocalDate
import javax.inject.Inject

class ReportsRemoteDataSource @Inject constructor(
    private val reportsApi: ReportsApi,
    private val fileManager: ReportFileManager
) : ReportsDataSource {

    override fun getReportToken(startDate: LocalDate, endDate: LocalDate): Single<TokenDto> =
        reportsApi.getReportToken(
            startDate = startDate.toStringWithFormat(DATE_PATTERN),
            endDate = endDate.toStringWithFormat(DATE_PATTERN)
        )

    override fun downloadReport(token: String, fileName: String): Single<Uri> =
        reportsApi.downloadReport(token)
            .map { fileManager.saveReport(fileName, it) }

    companion object {
        private const val DATE_PATTERN = "yyyyMMdd"
    }
}
