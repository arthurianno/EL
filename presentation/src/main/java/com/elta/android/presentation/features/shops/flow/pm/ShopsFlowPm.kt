package com.elta.android.presentation.features.shops.flow.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class ShopsFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    override fun navigateToLaunchScreen() {
        router.newRootScreen(Screens.ShopsStart)
    }
}