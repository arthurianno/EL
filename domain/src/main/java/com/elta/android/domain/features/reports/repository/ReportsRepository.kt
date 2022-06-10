package com.elta.android.domain.features.reports.repository

import android.net.Uri
import com.elta.android.domain.features.reports.model.Range
import io.reactivex.Single

interface ReportsRepository {

    fun getReport(range: Range): Single<Uri>
}
