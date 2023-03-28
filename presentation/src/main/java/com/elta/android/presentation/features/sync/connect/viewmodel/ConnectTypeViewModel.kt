package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectTypeViewState
import javax.inject.Inject

class ConnectTypeViewModel @Inject constructor() :
    BaseViewModel<ConnectTypeViewState, ConnectAction>() {
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

    fun connectByPin() {
        router.navigateTo(
            if (state.value.isOnBoarding) {
                Screens.FromOnBoardingConnectDeviceByPin
            } else {
                Screens.FromOtherConnectDeviceByPin
            }
        )
    }

    fun connectByDmc() {
        sendAction(ConnectAction.ConnectByDmc)
    }

    override fun reduceStateByAction(
        currentState: ConnectTypeViewState,
        action: Action
    ): ConnectTypeViewState {
        when (action) {
            is AppAction.BackPressure -> backClick()
            is ConnectAction.ConnectByPin -> router.navigateTo(
                if (state.value.isOnBoarding) {
                    Screens.FromOnBoardingConnectDeviceByPin
                } else {
                    Screens.FromOtherConnectDeviceByPin
                }
            )

            is ConnectAction.ConnectByDmc -> router.navigateTo(Screens.HowToConnectScreen(state.value.isOnBoarding))
            is ConnectAction.NeedHelp -> router.navigateTo(Screens.ConnectHelpScreen)
        }
        return currentState
    }
}
