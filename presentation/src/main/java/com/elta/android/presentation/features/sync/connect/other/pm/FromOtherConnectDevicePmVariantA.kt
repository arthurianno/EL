package com.elta.android.presentation.features.sync.connect.other.pm

import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCaseVariantA
import com.elta.android.domain.features.devices.interactor.CheckConnectedDevicesUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCaseVariantA
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCaseVariantA
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePmVariantA
import javax.inject.Inject

class FromOtherConnectDevicePmVariantA @Inject constructor(
    syncWithGlucometerUseCase: SyncWithGlucometerUseCaseVariantA,
    addNewDeviceUseCase: AddNewDeviceUseCaseVariantA,
    findGlucometersUseCase: FindGlucometersUseCaseVariantA,
    updateUserInfoUseCase: UpdateUserInfoUseCase,
    checkConnectedDevicesUseCase: CheckConnectedDevicesUseCase,
    appMetric: AppMetricTracker,
    services: ServiceFacade
) : ConnectDevicePmVariantA(
    syncWithGlucometerUseCase,
    addNewDeviceUseCase,
    findGlucometersUseCase,
    checkConnectedDevicesUseCase,
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
                AnalyticsEventParam.SOURCE to SOURCE_PROFILE
            )
            .subscribe()
            .untilDestroy()
    }

    override fun navigateToApp(i: Unit) {
        bus.event(Events.DeviceChanged)
        router.newRootFlow(Screens.HomeFlowVariantA)
    }
}