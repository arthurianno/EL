package com.elta.android.presentation.features.devices.info.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class DeviceInfoPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services)