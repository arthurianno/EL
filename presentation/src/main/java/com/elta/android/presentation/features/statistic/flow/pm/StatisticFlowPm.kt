package com.elta.android.presentation.features.statistic.flow.pm

import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class StatisticFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    override fun navigateToLaunchScreen() {
    }
}