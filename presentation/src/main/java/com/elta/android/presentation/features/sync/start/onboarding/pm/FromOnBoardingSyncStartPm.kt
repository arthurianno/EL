package com.elta.android.presentation.features.sync.start.onboarding.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.start.base.pm.SyncStartPm
import javax.inject.Inject

class FromOnBoardingSyncStartPm @Inject constructor(
    services: ServiceFacade
) : SyncStartPm(services) {

    override fun navigateToConnectDeviceScreen(i: Unit) {
        router.navigateTo(Screens.FromOnBoardingConnectDevice)
    }
}