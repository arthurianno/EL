package com.elta.android.presentation.features.devices.all.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.devices.all.ui.builder.DevicesOptionsItemsBuilder
import javax.inject.Inject

class DevicesPm @Inject constructor(
    private val itemsBuilder: DevicesOptionsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services)