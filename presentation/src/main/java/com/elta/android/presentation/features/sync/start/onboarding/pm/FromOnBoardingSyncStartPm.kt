package com.elta.android.presentation.features.sync.start.onboarding.pm

import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.start.base.pm.SyncStartPm
import javax.inject.Inject

class FromOnBoardingSyncStartPm @Inject constructor(
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    services: ServiceFacade
) : SyncStartPm(services) {

    override fun navigateToConnectDeviceScreen(i: Unit) {
        // fixme Variant A : improved_enabling_location
        val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
        val screen = if (improvedEnablingLocation) Screens.FromOnBoardingConnectDeviceByPin else Screens.FromOnBoardingConnectDeviceByPinVariantA
        router.navigateTo(screen)
    }
}
