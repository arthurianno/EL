package com.elta.android.presentation.features.auth.flow.pm

import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class AuthFlowPm @Inject constructor(
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    services: ServiceFacade
) : BaseFlowPm(services) {

    override fun navigateToLaunchScreen() {
        // fixme Variant A : recovery_account

        val isNewRecovery = getFeatureConfigUseCase.invoke().recoveryAccount
        val screen = if (isNewRecovery) Screens.Login
        else Screens.LoginVariantA
        router.newRootScreen(screen)
    }
}
