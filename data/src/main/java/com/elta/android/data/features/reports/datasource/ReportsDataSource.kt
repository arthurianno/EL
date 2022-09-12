package com.elta.android.data.features.reports.datasource

import android.net.Uri
import com.elta.android.data.features.reports.dto.TokenDto
import io.reactivex.Single
import org.threeten.bp.LocalDate

interface ReportsDataSource {

    fun getReportToken(startDate: LocalDate, endDate: LocalDate): Single<TokenDto>

    fun downloadReport(token: String, fileName: String): Single<Uri>
}
