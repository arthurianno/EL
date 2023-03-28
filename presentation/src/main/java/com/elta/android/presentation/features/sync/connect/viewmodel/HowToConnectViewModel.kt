package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.HowToConnectViewState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import javax.inject.Inject

@ExperimentalCameraProviderConfiguration
@OptIn(ExperimentalPermissionsApi::class)
class HowToConnectViewModel @Inject constructor() :
    BaseViewModel<HowToConnectViewState, ConnectAction>() {
    override fun createInitState(): HowToConnectViewState =
        HowToConnectViewState(
            isOnBoarding = false
        )

    val appTopBar = BaseAppTopBarWidgetModel()
    val downButton = DownButtonWidgetModel()

    override val widgets = listOf(
        appTopBar,
        downButton
    ).actionObserve()

    override fun reduceStateByAction(
        currentState: HowToConnectViewState,
        action: Action
    ): HowToConnectViewState {
        when (action) {
            is AppAction.BackPressure -> backClick()
            is ConnectAction.OpenConnectingScreen -> if (action.permissionStatus.isGranted) {
                router.navigateTo(Screens.ScannerDmcScreen(state.value.isOnBoarding))
            } else {
                sendEvent(PermissionEvent.Camera())
            }
        }
        return currentState
    }

    override fun handleFragmentArguments(arguments: Bundle) {
        reduceState {
            state.value.copy(
                isOnBoarding = arguments.getBoolean(
                    IS_ON_BOARDING_ARGUMENT_NAME
                )
            )
        }
    }
}
