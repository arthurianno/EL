package com.elta.android.presentation.features.sync.connect.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class ConnectDevicePm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    val mainAction = Action<Unit>()
    val skipAction = Action<Unit>()
}