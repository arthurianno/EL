package com.elta.android.presentation.features.sync.start.other.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.start.base.pm.SyncStartPm
import javax.inject.Inject

class FromOtherSyncStartPm @Inject constructor(
    services: ServiceFacade
) : SyncStartPm(services) {

    override fun navigateToConnectDeviceScreen(i: Unit) =
        router.navigateTo(Screens.FromOtherConnectDeviceByPin)
}
