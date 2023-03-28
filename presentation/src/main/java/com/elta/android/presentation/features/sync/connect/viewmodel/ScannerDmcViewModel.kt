package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import androidx.compose.ui.unit.DpSize
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectMainEvent
import com.elta.android.presentation.features.sync.connect.model.ScannerDmcViewState
import com.elta.android.presentation.features.sync.connect.model.ScannerState
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val SCANNER_ERROR_SHOWING_DELAY_MILLIS = 1000L

class ScannerDmcViewModel @Inject constructor() :
    BaseViewModel<ScannerDmcViewState, ConnectAction>() {
    override fun createInitState(): ScannerDmcViewState =
        ScannerDmcViewState(
            scannerState = ScannerState.Info,
            isOnBoarding = false,
            cropRect = DpSize.Zero
        )

    internal val connectByPinButton = DownButtonWidgetModel()
    internal val appTopBar = BaseAppTopBarWidgetModel()

    override val widgets = listOf(
        connectByPinButton,
        appTopBar
    ).actionObserve()

    fun setCropSize(cropRect: DpSize) {
        reduceState { state.value.copy(cropRect = cropRect) }
    }

    fun setScannerError() {
        launch {
            reduceState { state.value.copy(scannerState = ScannerState.Error) }
            delay(SCANNER_ERROR_SHOWING_DELAY_MILLIS)
            reduceState { state.value.copy(scannerState = ScannerState.Info) }
        }
    }

    fun startConnecting(pin: Int, name: String) {
        router.navigateTo(Screens.ConnectingScreen(state.value.isOnBoarding, pin, name))
    }

    override fun reduceStateByAction(
        currentState: ScannerDmcViewState,
        action: Action
    ): ScannerDmcViewState = run {
        when (action) {
            is ConnectAction.NeedHelp -> reloadSheetContent(ScannerState.Help)
            is ConnectAction.CloseHelp -> reloadSheetContent(ScannerState.Info)
            else -> {
                when (action) {
                    is ConnectAction.ConnectByPin -> connectByPin()
                    is AppAction.BackPressure -> router.exit()
                }
                currentState
            }
        }
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

    private fun reloadSheetContent(newContentType: ScannerState): ScannerDmcViewState = run {
        sendEvent(ConnectMainEvent.ShowSheet())
        state.value.copy(scannerState = newContentType)
    }

    private fun connectByPin() {
        sendEvent(ConnectMainEvent.HideSheet())
        router.navigateTo(
            if (state.value.isOnBoarding) {
                Screens.FromOnBoardingConnectDeviceByPin
            } else {
                Screens.FromOtherConnectDeviceByPin
            }
        )
    }
}
