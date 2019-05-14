package com.elta.android.presentation.features.devices.info.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import javax.inject.Inject

class DeviceInfoPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    override fun onCreate() {
        super.onCreate()

        // todo only for testing
        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { arrayListOf(DeviceInfoItem("dmdmd", "sssm")) }
            .doOnNext(items.consumer)
            .subscribe()
            .untilDestroy()
    }
}