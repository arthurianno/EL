package com.elta.android.presentation.features.sync.connect.onboarding.pm

import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCase
import com.elta.android.domain.features.devices.interactor.CheckConnectedDevicesUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.diary.home.interactor.GetLocationNeededUseCase
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import javax.inject.Inject

class FromOnBoardingConnectDevicePm @Inject constructor(
    syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    addNewDeviceUseCase: AddNewDeviceUseCase,
    findGlucometersUseCase: FindGlucometersUseCase,
    updateUserInfoUseCase: UpdateUserInfoUseCase,
    getLocationNeededUseCase: GetLocationNeededUseCase,
    checkConnectedDevicesUseCase: CheckConnectedDevicesUseCase,
    appMetric: AppMetricTracker,
    services: ServiceFacade
) : ConnectDevicePm(
    syncWithGlucometerUseCase,
    addNewDeviceUseCase,
    findGlucometersUseCase,
    checkConnectedDevicesUseCase,
    getLocationNeededUseCase,
    updateUserInfoUseCase,
    appMetric,
    services
) {

    override fun onCreate() {
        super.onCreate()
        connectState.observable
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
