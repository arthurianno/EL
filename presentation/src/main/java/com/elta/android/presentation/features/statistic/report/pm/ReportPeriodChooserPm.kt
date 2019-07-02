package com.elta.android.presentation.features.statistic.report.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class ReportPeriodChooserPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val actionButtonEnabledCommand = State(false)
    val mainAction = Action<Unit>()
    val closeDialogCommand = Command<Unit>(bufferSize = 1)

}