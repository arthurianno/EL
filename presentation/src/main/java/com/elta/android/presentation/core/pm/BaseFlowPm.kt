package com.elta.android.presentation.core.pm

import android.support.annotation.CallSuper

abstract class BaseFlowPm(
    services: ServiceFacade
) : BasePm(services) {

    val launchScreenAction = Action<Unit>()

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        launchScreenAction
            .observable
            .doOnNext { navigateToLaunchScreen() }
            .subscribe()
            .untilDestroy()
    }

    abstract fun navigateToLaunchScreen()
}