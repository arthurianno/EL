package com.nullgr.android.presentation.features.app.pm

import com.nullgr.android.presentation.Screens
import com.nullgr.android.presentation.core.pm.BasePm
import com.nullgr.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class AppPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val coldStartAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        coldStartAction.observable
            .doOnNext { router.newRootScreen(Screens.OnBoardingFlow) }
            .retry()
            .subscribe()
            .untilDestroy()
    }
}