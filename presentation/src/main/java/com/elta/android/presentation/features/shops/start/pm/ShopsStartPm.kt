package com.elta.android.presentation.features.shops.start.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action
import javax.inject.Inject

class ShopsStartPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val findShopAction = action<Unit>()
    val skipAction = action<Unit>()

    override fun onCreate() {
        super.onCreate()

        findShopAction.observable
            .doOnNext(::navigateToMapScreen)
            .subscribe()
            .untilDestroy()

        skipAction.observable
            .doOnNext(::navigateToMainScreen)
            .subscribe()
            .untilDestroy()
    }

    private fun navigateToMapScreen(i: Unit) {
        router.navigateTo(Screens.ShopsMap)
    }

    private fun navigateToMainScreen(i: Unit) {
        router.newRootFlow(Screens.HomeFlow)
    }
}
