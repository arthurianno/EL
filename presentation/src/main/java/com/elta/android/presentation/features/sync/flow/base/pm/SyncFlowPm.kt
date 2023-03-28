package com.elta.android.presentation.features.sync.flow.base.pm

import com.elta.android.presentation.core.navigation.support.SupportAppScreen
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade

@Deprecated("Данный flow помечен на удаление")
abstract class SyncFlowPm constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    protected abstract val screen: SupportAppScreen

    override fun navigateToLaunchScreen() {
        router.newRootScreen(screen)
    }
}
