package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.ConnectingPathParam
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonClick
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectStartViewState
import javax.inject.Inject

class ConnectStartViewModel @Inject constructor(
    private val appMetric: AppMetricTracker
) : BaseViewModel<ConnectStartViewState>() {
    override fun createInitState() = ConnectStartViewState(
        isOnBoarding = true
    )

    internal val appTopBar = BaseAppTopBarWidgetModel()
    internal val downButton = DownButtonWidgetModel()

    override val widgets = listOf(
        appTopBar,
        downButton
    ).actionObserve()

    override fun handleFragmentArguments(arguments: Bundle) {
        val isOnboarding = arguments.getBoolean(IS_ON_BOARDING_ARGUMENT_NAME)

        val eventParam = if (isOnboarding) ConnectingPathParam.ONBOARDING
        else ConnectingPathParam.SYNCHRONIZATION
        val eventName = AppMetricEvent.DeviceConnectingClick(eventParam)
        appMetric.trackEvent(eventName)

        reduceState {
            state.value.copy(isOnBoarding = isOnboarding)
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is DownButtonClick -> router.navigateTo(Screens.ConnectTypeScreen(state.value.isOnBoarding))
            is ConnectAction.SkipNextStep -> router.navigateTo(Screens.ShopsStart)
        }
        super.handleUserAction(action)
    }
}
