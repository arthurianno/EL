package com.elta.android.presentation.features.sync.connect.onboarding.pm

import com.elta.android.domain.features.devices.interactor.ConnectDeviceUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import javax.inject.Inject

class FromOnBoardingConnectDevicePm @Inject constructor(
    syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    connectDeviceUseCase: ConnectDeviceUseCase,
    findGlucometersUseCase: FindGlucometersUseCase,
    updateUserInfoUseCase: UpdateUserInfoUseCase,
    services: ServiceFacade
) : ConnectDevicePm(
    syncWithGlucometerUseCase,
    connectDeviceUseCase,
    findGlucometersUseCase,
    updateUserInfoUseCase,
    services
) {

    override fun onCreate() {
        super.onCreate()
        mstate.observable
            .filter { it == ViewState.CONNECTED }
            .trackEvent(
                AnalyticsEventType.GLUCOMETER_ADD,
                AnalyticsEventParam.SOURCE to SOURCE_ON_BOARDING
            )
            .subscribe()
            .untilDestroy()
    }

    override fun navigateToApp(i: Unit) =
        router.newRootFlow(Screens.HomeFlow)

    companion object {
        private const val SOURCE_ON_BOARDING = "onboarding"
    }
}
