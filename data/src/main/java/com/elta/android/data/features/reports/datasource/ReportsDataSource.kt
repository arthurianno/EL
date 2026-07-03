package com.elta.android.data.features.reports.datasource

import android.net.Uri
import com.elta.android.domain.features.reports.model.ReportType
import com.elta.android.domain.features.user.model.GlucoseFormat
import io.reactivex.Single
import org.threeten.bp.LocalDate

interface ReportsDataSource {

    fun downloadReport(
        startDate: LocalDate,
        endDate: LocalDate,
        glucoseFormat: GlucoseFormat,
        reportType: ReportType,
        fileName: String
    ): Single<Uri>
}
