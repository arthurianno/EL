package com.elta.android.presentation.features.main.flow.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class MainFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    override fun navigateToLaunchScreen() {
        router.newRootScreen(Screens.MainRecordsScreen)
    }
}