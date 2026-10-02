package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import android.os.CountDownTimer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.elta.android.domain.features.devices.interactor.GetGlucometersUseCase
import com.elta.android.domain.features.devices.model.matchesTargetGlucometerName
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ScannerDmcViewState
import com.elta.android.presentation.features.sync.connect.model.ScannerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx2.await
import javax.inject.Inject

private const val SCANNER_ERROR_SHOWING_DELAY_MILLIS = 1000L
private const val CLOSE_TIMER_DELAY_MILLIS = 60000L

class ScannerDmcViewModel @Inject constructor(
    private val getGlucometersUseCase: GetGlucometersUseCase,
    private val appMetric: AppMetricTracker
) : BaseViewModel<ScannerDmcViewState>(), LifecycleEventObserver {
    override fun createInitState(): ScannerDmcViewState =
        ScannerDmcViewState(
            scannerState = ScannerState.Info,
            isOnBoarding = false
        )

    private var closeTimer: CountDownTimer? = null

    private var scannerJob: Job? = null

    init {
        appMetric.trackEvent(AppMetricEvent.CameraScanningScreen)
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

    override fun handleUserAction(action: Action) {
        when (action) {
            is AppAction.BackPressure -> backClick()
            is ConnectAction.OnDmcReceived -> launch { startConnecting(action.pin, action.name) }
            is ConnectAction.ScannerError -> setScannerError(ScannerState.Error)
            is ConnectAction.ConnectByPin -> connectByPin()
        }
    }

    private fun connectByPin() {
        router.navigateTo(
            if (state.value.isOnBoarding) {
                Screens.FromOnBoardingConnectDeviceByPin
            } else {
                Screens.FromOtherConnectDeviceByPin
            }
        )
    }

    override fun reduceStateByAction(
        currentState: ScannerDmcViewState,
        action: Action
    ): ScannerDmcViewState = run {
        when (action) {
            is ConnectAction.NeedHelp -> reloadSheetContent(ScannerState.Help)
            is ConnectAction.CloseHelp -> reloadSheetContent(ScannerState.Info)
            else -> currentState
        }
    }

    override fun backClick() {
        scannerJob?.cancel()
        super.backClick()
    }

    private fun setScannerError(errorState: ScannerState) {
        if (scannerJob == null || scannerJob?.isCancelled == true) {
            restartCloseTime()
            scannerJob = launch {
                reduceState { state.value.copy(scannerState = errorState) }
                delay(SCANNER_ERROR_SHOWING_DELAY_MILLIS)
                reduceState { state.value.copy(scannerState = ScannerState.Info) }
                delay(SCANNER_ERROR_SHOWING_DELAY_MILLIS)

                scannerJob?.cancel()
            }
        }
    }

    private suspend fun startConnecting(pin: String, name: String) {
        val connectedGlucometers = getGlucometersUseCase.execute()
            .await()
            .map { it.first.name }
        if (connectedGlucometers.any { matchesTargetGlucometerName(it, name) }) {
            setScannerError(ScannerState.AlreadyConnected)
        } else {
            router.navigateTo(Screens.ConnectingScreen(state.value.isOnBoarding, pin, name))
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> createStopTimer()
            Lifecycle.Event.ON_RESUME -> restartCloseTime()
            Lifecycle.Event.ON_PAUSE -> stopCloseTime()
            else -> Unit
        }
    }

    private fun restartCloseTime() {
        closeTimer?.cancel()
        closeTimer?.start()
    }

    private fun stopCloseTime() {
        closeTimer?.cancel()
        closeTimer = null
    }

    private fun createStopTimer() {
        closeTimer = object : CountDownTimer(CLOSE_TIMER_DELAY_MILLIS, CLOSE_TIMER_DELAY_MILLIS) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                backClick()
            }
        }
    }

    private fun reloadSheetContent(newContentType: ScannerState): ScannerDmcViewState = run {
        state.value.copy(scannerState = newContentType)
    }
}
