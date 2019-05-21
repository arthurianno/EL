package com.elta.android.presentation.features.devices.firmware.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class FirmwarePm @Inject constructor(
    services: ServiceFacade
) : BasePm(services)