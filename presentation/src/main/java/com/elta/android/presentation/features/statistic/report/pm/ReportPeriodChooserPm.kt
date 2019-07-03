package com.elta.android.presentation.features.statistic.report.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import org.threeten.bp.LocalDate
import javax.inject.Inject

class ReportPeriodChooserPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val actionButtonEnabledCommand = State(false)
    val mainAction = Action<Unit>()
    val closeDialogCommand = Command<Unit>(bufferSize = 1)
    val selectDateAction = Action<LocalDate>()
    val selectedRangeState = State(getRange())

    override fun onCreate() {
        super.onCreate()

        selectDateAction.observable
            .map(::getRange)
            .subscribe(selectedRangeState.consumer)
            .untilDestroy()
    }

    // TODO: to domain
    private fun getRange(now: LocalDate = LocalDate.now()): Range = Range(start = now.minusDays(13), end = now)

    data class Range(val start: LocalDate, val end: LocalDate)
}