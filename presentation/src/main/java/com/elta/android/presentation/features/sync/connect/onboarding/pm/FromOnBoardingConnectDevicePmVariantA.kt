package com.elta.android.presentation.features.sync.connect.onboarding.pm

import android.content.Context
import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCaseVariantA
import com.elta.android.domain.features.devices.interactor.CheckConnectedDevicesUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCaseVariantA
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCaseVariantA
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePmVariantA
import javax.inject.Inject

class FromOnBoardingConnectDevicePmVariantA @Inject constructor(
    syncWithGlucometerUseCase: SyncWithGlucometerUseCaseVariantA,
    addNewDeviceUseCase: AddNewDeviceUseCaseVariantA,
    findGlucometersUseCase: FindGlucometersUseCaseVariantA,
    updateUserInfoUseCase: UpdateUserInfoUseCase,
    checkConnectedDevicesUseCase: CheckConnectedDevicesUseCase,
    appMetric: AppMetricTracker,
    context: Context,
    getScreenConfigFromCache: GetScreenConfigFromCache,
    services: ServiceFacade
) : ConnectDevicePmVariantA(
    syncWithGlucometerUseCase,
    addNewDeviceUseCase,
    findGlucometersUseCase,
    checkConnectedDevicesUseCase,
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
                AnalyticsEventParam.SOURCE to SOURCE_ON_BOARDING
            )
            .subscribe()
            .untilDestroy()
    }

    override fun navigateToApp(i: Unit) =
        router.newRootFlow(Screens.HomeFlowVariantA)

    companion object {
        private const val SOURCE_ON_BOARDING = "onboarding"
    }
}
