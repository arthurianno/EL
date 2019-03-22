package com.elta.android.presentation.features.sync.start.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class SyncStartPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val mainAction = Action<Unit>()
    val skipAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        mainAction.observable
            .doOnNext(::navigateToBluetoothScreen)
            .subscribe()
            .untilDestroy()

        skipAction.observable
            .doOnNext(::navigateToShopsFlow)
            .subscribe()
            .untilDestroy()
    }

    private fun navigateToBluetoothScreen(i: Unit) {
        router.navigateTo(Screens.BluetoothScreen)
    }

    private fun navigateToShopsFlow(i: Unit) {
        router.newRootFlow(Screens.ShopsFlow)
    }
}