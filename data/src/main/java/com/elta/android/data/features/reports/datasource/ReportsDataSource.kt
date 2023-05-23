package com.elta.android.data.features.reports.datasource

import android.net.Uri
import com.elta.android.data.features.reports.dto.TokenDto
import com.elta.android.domain.features.user.model.GlucoseFormat
import io.reactivex.Single
import org.threeten.bp.LocalDate

interface ReportsDataSource {

    fun getReportToken(
        startDate: LocalDate,
        endDate: LocalDate,
        glucoseFormat: GlucoseFormat
    ): Single<TokenDto>

    fun downloadReport(token: String, fileName: String): Single<Uri>
}
