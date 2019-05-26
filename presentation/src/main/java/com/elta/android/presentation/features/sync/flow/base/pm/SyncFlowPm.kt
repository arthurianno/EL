package com.elta.android.presentation.features.sync.flow.base.pm

import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import ru.terrakok.cicerone.android.support.SupportAppScreen

abstract class SyncFlowPm constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    protected abstract val screen: SupportAppScreen

    override fun navigateToLaunchScreen() {
        router.newRootScreen(screen)
    }
}