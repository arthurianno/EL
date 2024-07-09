package com.elta.android.presentation.features.sync.start.base.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action

abstract class SyncStartPm constructor(
    services: ServiceFacade
) : BasePm(services) {

    val mainAction = action<Unit>()
    val skipAction = action<Unit>()

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
        // todo: SalepointHide
        // скрываем точки продаж пока пока не примем решение что с ними делать
//        router.newRootFlow(Screens.ShopsFlow)
    }
}
