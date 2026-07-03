package com.elta.android.domain.features.reports.interactor

import android.net.Uri
import com.elta.android.domain.features.reports.model.Range
import com.elta.android.domain.features.reports.model.ReportType
import com.elta.android.domain.features.reports.repository.ReportsRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetReportUseCase @Inject constructor(
    private val repo: ReportsRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Uri, GetReportUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<Uri> {
        val verifiedParams = checkNotNull(params)
        return repo.getReport(verifiedParams.range, verifiedParams.reportType)
    }

    data class Params(val range: Range, val reportType: ReportType)
}
