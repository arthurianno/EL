package com.elta.android.presentation.features.devices.all.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.devices.all.ui.builder.DevicesOptionsItemsBuilder
import com.nullgr.core.rx.bindEmpty
import javax.inject.Inject

class DevicesPm @Inject constructor(
    private val itemsBuilder: DevicesOptionsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    override fun onCreate() {
        super.onCreate()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { itemsBuilder.buildItems().toList() }
            .bindEmpty(emptyControl.visibilityState.consumer)
            .subscribe(items.consumer)
            .untilDestroy()
    }
}