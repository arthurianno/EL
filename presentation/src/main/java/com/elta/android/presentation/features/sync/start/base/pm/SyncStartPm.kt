package com.elta.android.presentation.features.sync.start.base.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade

abstract class SyncStartPm constructor(
    services: ServiceFacade
) : BasePm(services) {

    val mainAction = Action<Unit>()
    val skipAction = Action<Unit>()

    abstract fun navigateToConnectDeviceScreen(i: Unit)

    override fun onCreate() {
        super.onCreate()

        mainAction.observable
            .doOnNext(::navigateToConnectDeviceScreen)
            .subscribe()
            .untilDestroy()

        skipAction.observable
            .doOnNext(::navigateToShopsFlow)
            .subscribe()
            .untilDestroy()
    }

    private fun navigateToShopsFlow(i: Unit) {
        router.newRootFlow(Screens.ShopsFlow)
    }
}