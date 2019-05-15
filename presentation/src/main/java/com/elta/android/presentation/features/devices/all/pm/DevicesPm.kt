package com.elta.android.presentation.features.devices.all.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class DevicesPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services)