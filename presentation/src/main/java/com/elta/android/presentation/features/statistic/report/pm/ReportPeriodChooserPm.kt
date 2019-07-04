package com.elta.android.presentation.features.statistic.report.pm

import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.reports.interactor.buildRange
import com.elta.android.domain.features.reports.model.Range
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
    val selectedRangeState = State(buildRange())
    val titleState = State<String>()

    override fun onCreate() {
        super.onCreate()

        selectDateAction.observable
            .map(::buildRange)
            .subscribe(selectedRangeState.consumer)
            .untilDestroy()

        selectedRangeState.observable
            .map(::formatRange)
            .subscribe(titleState.consumer)
            .untilDestroy()
    }

    private fun formatRange(range: Range): String =
        "${range.start.toStringWithFormat(FORMAT)} - ${range.end.toStringWithFormat(FORMAT)}"

    private companion object {
        const val FORMAT = "d LLLL"
    }
}