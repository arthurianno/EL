package com.elta.android.presentation.features.shops.start.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import timber.log.Timber
import javax.inject.Inject

class ShopsStartPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val findShopAction = Action<Unit>()
    val skipAction = Action<Unit>()

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
        Timber.d("navigateToMainScreen")
    }
}