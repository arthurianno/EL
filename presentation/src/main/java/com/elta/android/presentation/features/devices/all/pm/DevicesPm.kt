package com.elta.android.presentation.features.devices.all.pm

import com.elta.android.domain.features.devices.interactor.GetGlucometersUseCase
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.devices.all.ui.builder.DevicesOptionsItemsBuilder
import com.nullgr.core.rx.bindEmpty
import javax.inject.Inject

class DevicesPm @Inject constructor(
    private val getGlucometersUseCase: GetGlucometersUseCase,
    private val itemsBuilder: DevicesOptionsItemsBuilder,
    services: ServiceFacade
) : BaseListPm(services) {

    private val getGlucometers = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        bindClicks()
        bindGlucometersAction()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(getGlucometers.consumer)
            .untilDestroy()
    }

    private fun bindClicks() =
        bus.clicks<Clicks.ActiveDeviceItemClicked>()
            .map { Screens.DeviceInfo }
            .doOnNext(router::navigateTo)
            .subscribe()
            .untilDestroy()

    private fun bindGlucometersAction() =
        getGlucometers.observable
            .skipWhileInProgress()
            .flatMap {
                getGlucometersUseCase.execute()
                    .bindProgress()
                    .bindEmpty(emptyControl.visibilityState.consumer)
                    .map(itemsBuilder::buildItems)
                    .doOnNext(items.consumer)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
}