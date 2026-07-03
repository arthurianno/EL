package com.elta.android.domain.features.reports.repository

import android.net.Uri
import com.elta.android.domain.features.reports.model.Range
import com.elta.android.domain.features.reports.model.ReportType
import io.reactivex.Single

interface ReportsRepository {

    fun getReport(range: Range, reportType: ReportType): Single<Uri>
}
