package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingTypeParam
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.viewmodel.ComposeScreenConfigurable
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectTypeViewState
import javax.inject.Inject

class ConnectTypeViewModel @Inject constructor(
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    private val analytics: Analytics,
    private val appMetric: AppMetricTracker,
    private val getScreenCacheConfigUseCase : GetScreenConfigFromCache,
) : BaseViewModel<ConnectTypeViewState>(), ComposeScreenConfigurable {
    override fun createInitState(): ConnectTypeViewState =
        ConnectTypeViewState(
            isOnBoarding = true
        )

    override val screenConfigKey = "device-screen"
    override val getScreenConfigUseCase = getScreenCacheConfigUseCase

    val appTopBar = BaseAppTopBarWidgetModel()

    override val widgets = listOf(
        appTopBar
    ).actionObserve()

    override fun handleFragmentArguments(arguments: Bundle) {
        appMetric.trackEvent(AppMetricEvent.DeviceConnectingScreen)
        reduceState {
            state.value.copy(
                isOnBoarding = arguments.getBoolean(
                    IS_ON_BOARDING_ARGUMENT_NAME
                )
            )
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is AppAction.BackPressure -> backClick()
            is ConnectAction.ConnectByPin -> connectByPin()
            is ConnectAction.ConnectByDmc -> connectByDmc()
            is ConnectAction.NeedHelp -> router.navigateTo(Screens.ConnectHelpScreen)
        }
    }

    private fun connectByDmc() {
        appMetric.trackEvent(AppMetricEvent.ConnectingOptionClick(ConnectingTypeParam.DMC_SCAN))
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.SCAN_DMC))
        // fixme Variant A : improved_enabling_location
        val screen = if (getFeatureConfigUseCase.invoke().improvedEnablingLocation) Screens.HowToConnectScreen(state.value.isOnBoarding)
        else Screens.HowToConnectScreenVariantA(state.value.isOnBoarding)
        router.navigateTo(screen)
    }

    private fun connectByPin() {
        appMetric.trackEvent(AppMetricEvent.ConnectingOptionClick(ConnectingTypeParam.PIN_ENTER))
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.PIN_CONNECTION))
            // fixme variant a improved_enabling_location
        val (fromOnboardingScreen, fromOtherConnectScreen) = if (getFeatureConfigUseCase.invoke().improvedEnablingLocation) {
            Screens.FromOnBoardingConnectDeviceByPin to Screens.FromOtherConnectDeviceByPin
        } else {
            Screens.FromOnBoardingConnectDeviceByPinVariantA to Screens.FromOtherConnectDeviceByPinVariantA
        }
        router.navigateTo(
            if (state.value.isOnBoarding) {
                fromOnboardingScreen
            } else {
                fromOtherConnectScreen
            }
        )
    }
}
