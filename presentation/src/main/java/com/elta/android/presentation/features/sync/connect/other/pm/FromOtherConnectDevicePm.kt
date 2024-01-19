package com.elta.android.presentation.features.sync.connect.other.pm

import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import javax.inject.Inject

private const val SOURCE_PROFILE = "profile"

class FromOtherConnectDevicePm @Inject constructor(
    syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    addNewDeviceUseCase: AddNewDeviceUseCase,
    findGlucometersUseCase: FindGlucometersUseCase,
    updateUserInfoUseCase: UpdateUserInfoUseCase,
    services: ServiceFacade
) : ConnectDevicePm(
    syncWithGlucometerUseCase,
    addNewDeviceUseCase,
    findGlucometersUseCase,
    updateUserInfoUseCase,
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
