package com.elta.android.presentation.core.pm

import android.support.annotation.CallSuper
import me.dmdev.rxpm.action

abstract class BaseFlowPm(
    services: ServiceFacade
) : BasePm(services) {

    val launchScreenAction = action<Unit>()

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
