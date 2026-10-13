package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingPathParam
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingTypeParam
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectTypeViewState
import javax.inject.Inject

class ConnectTypeViewModel @Inject constructor(
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    private val analytics: Analytics,
    private val appMetric: AppMetricTracker
) : BaseViewModel<ConnectTypeViewState>() {
    override fun createInitState() = ConnectTypeViewState(isOnBoarding = false)

    override fun handleFragmentArguments(arguments: Bundle) {
        val isOnBoarding = arguments.getBoolean(IS_ON_BOARDING_ARGUMENT_NAME)
        reduceState { state.value.copy(isOnBoarding = isOnBoarding) }
        if (isOnBoarding) {
            appMetric.trackEvent(AppMetricEvent.DeviceConnectingClick(ConnectingPathParam.ONBOARDING))
        }
        appMetric.trackEvent(AppMetricEvent.DeviceConnectingScreen)
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            AppAction.BackPressure -> backClick()
            ConnectAction.ConnectByDmc -> connectByDmc()
            ConnectAction.ConnectByPin -> connectByPin()
            ConnectAction.SkipNextStep -> connectLater()
            ConnectAction.NeedHelp -> router.navigateTo(Screens.ConnectHelpScreen)
        }
    }

    override fun backClick() {
        if (state.value.isOnBoarding) connectLater() else router.exit()
    }

    private fun connectByDmc() {
        appMetric.trackEvent(AppMetricEvent.ConnectingOptionClick(ConnectingTypeParam.DMC_SCAN))
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.SCAN_DMC))
        val screen = if (getFeatureConfigUseCase().improvedEnablingLocation) {
            Screens.HowToConnectScreen(state.value.isOnBoarding)
        } else {
            Screens.HowToConnectScreenVariantA(state.value.isOnBoarding)
        }
        router.navigateTo(screen)
    }

    private fun connectByPin() {
        appMetric.trackEvent(AppMetricEvent.ConnectingOptionClick(ConnectingTypeParam.PIN_ENTER))
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.PIN_CONNECTION))
        val improvedEnablingLocation = getFeatureConfigUseCase().improvedEnablingLocation
        val screen = when {
            improvedEnablingLocation && state.value.isOnBoarding -> Screens.FromOnBoardingConnectDeviceByPin
            improvedEnablingLocation -> Screens.FromOtherConnectDeviceByPin
            state.value.isOnBoarding -> Screens.FromOnBoardingConnectDeviceByPinVariantA
            else -> Screens.FromOtherConnectDeviceByPinVariantA
        }
        router.navigateTo(screen)
    }

    private fun connectLater() {
        if (!state.value.isOnBoarding) return
        val screen = if (getFeatureConfigUseCase().improvedEnablingLocation) Screens.HomeFlow
        else Screens.HomeFlowVariantA
        router.newRootScreen(screen)
    }
}
