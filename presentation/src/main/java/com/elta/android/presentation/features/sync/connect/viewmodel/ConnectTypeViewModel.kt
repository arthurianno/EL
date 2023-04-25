package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.core.Analytics
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectTypeViewState
import javax.inject.Inject

class ConnectTypeViewModel @Inject constructor(
    private val analytics: Analytics
) : BaseViewModel<ConnectTypeViewState>() {
    override fun createInitState(): ConnectTypeViewState =
        ConnectTypeViewState(
            isOnBoarding = true
        )

    val appTopBar = BaseAppTopBarWidgetModel()

    override val widgets = listOf(
        appTopBar
    ).actionObserve()

    override fun handleFragmentArguments(arguments: Bundle) {
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
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.SCAN_DMC))
        router.navigateTo(Screens.HowToConnectScreen(state.value.isOnBoarding))
    }

    private fun connectByPin() {
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.PIN_CONNECTION))
        router.navigateTo(
            if (state.value.isOnBoarding) {
                Screens.FromOnBoardingConnectDeviceByPin
            } else {
                Screens.FromOtherConnectDeviceByPin
            }
        )
    }
}
