package com.elta.android.presentation.features.sync.connect.other.pm

import android.content.Context
import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCase
import com.elta.android.domain.features.devices.interactor.CheckConnectedDevicesUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.diary.home.interactor.GetLocationNeededUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import javax.inject.Inject

// fixme Variant A : improved_enabling_location
const val SOURCE_PROFILE = "profile"

class FromOtherConnectDevicePm @Inject constructor(
    syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    addNewDeviceUseCase: AddNewDeviceUseCase,
    findGlucometersUseCase: FindGlucometersUseCase,
    updateUserInfoUseCase: UpdateUserInfoUseCase,
    checkConnectedDevicesUseCase: CheckConnectedDevicesUseCase,
    getLocationNeededUseCase: GetLocationNeededUseCase,
    appMetric: AppMetricTracker,
    context: Context,
    getScreenConfigFromCache: GetScreenConfigFromCache,
    services: ServiceFacade
) : ConnectDevicePm(
    syncWithGlucometerUseCase,
    addNewDeviceUseCase,
    findGlucometersUseCase,
    checkConnectedDevicesUseCase,
    getLocationNeededUseCase,
    updateUserInfoUseCase,
    appMetric,
    context,
    getScreenConfigFromCache,
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
        router.newRootFlow(Screens.HomeFlow)
    }
}
