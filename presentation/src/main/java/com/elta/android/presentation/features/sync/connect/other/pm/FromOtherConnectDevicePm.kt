package com.elta.android.presentation.features.sync.connect.other.pm

import com.elta.android.domain.features.devices.interactor.ConnectDeviceUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
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
    connectDeviceUseCase: ConnectDeviceUseCase,
    findGlucometersUseCase: FindGlucometersUseCase,
    services: ServiceFacade
) : ConnectDevicePm(
    syncWithGlucometerUseCase,
    connectDeviceUseCase,
    findGlucometersUseCase,
    services
) {

    override fun onCreate() {
        super.onCreate()
        mstate.observable
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
        router.backTo(Screens.Devices)
    }
}
