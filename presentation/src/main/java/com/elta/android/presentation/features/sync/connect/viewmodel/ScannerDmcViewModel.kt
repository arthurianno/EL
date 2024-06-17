package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import android.os.CountDownTimer
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val SCANNER_ERROR_SHOWING_DELAY_MILLIS = 1000L
private const val CLOSE_TIMER_DELAY_MILLIS = 60000L

class ScannerDmcViewModel @Inject constructor(
    private val analytics: Analytics,
    private val appMetric: AppMetricTracker
) : BaseViewModel<ScannerDmcViewState>(), LifecycleEventObserver {
    override fun createInitState(): ScannerDmcViewState =
        ScannerDmcViewState(
            scannerState = ScannerState.Info,
            isOnBoarding = false,
            cropRect = DpSize.Zero
        )

    private var closeTimer: CountDownTimer? = null

    private var scannerJob: Job? = null

    internal val connectByPinButton = DownButtonWidgetModel()
    internal val appTopBar = BaseAppTopBarWidgetModel()

    override val widgets = listOf(
        connectByPinButton,
        appTopBar
    ).actionObserve()

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
            is ConnectAction.ConnectByPin -> connectByPin()
            is AppAction.BackPressure -> backClick()
            is ConnectAction.StartConnecting -> startConnecting(action.pin, action.name)
            is ConnectAction.ScannerError -> setScannerError()
        }
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

    fun setCropSize(cropRect: DpSize) {
        reduceState { state.value.copy(cropRect = cropRect) }
    }

    private fun setScannerError() {
        if (scannerJob == null || scannerJob?.isCancelled == true) {
            restartCloseTime()
            scannerJob = launch {
                reduceState { state.value.copy(scannerState = ScannerState.Error) }
                delay(SCANNER_ERROR_SHOWING_DELAY_MILLIS)
                reduceState { state.value.copy(scannerState = ScannerState.Info) }
                delay(SCANNER_ERROR_SHOWING_DELAY_MILLIS)

                scannerJob?.cancel()
            }
        }
    }

    private fun startConnecting(pin: String, name: String) {
        router.navigateTo(Screens.ConnectingScreen(state.value.isOnBoarding, pin, name))
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
        sendEvent(ConnectMainEvent.ShowSheet())
        state.value.copy(scannerState = newContentType)
    }

    private fun connectByPin() {
        sendEvent(ConnectMainEvent.HideSheet())
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
