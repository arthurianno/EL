package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.domain.features.devices.interactor.ConnectDeviceUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.core.Analytics
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.features.sync.connect.GLUCOMETER_NAME_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.PIN_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectMainEvent
import com.elta.android.presentation.features.sync.connect.model.ConnectingStageType
import com.elta.android.presentation.features.sync.connect.model.ConnectingViewState
import com.nullgr.core.rx.RxBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val CONNECT_DEVICE_TIMEOUT_SEC = 60L

class ConnectingViewModel @Inject constructor(
    private val findGlucometers: FindGlucometersUseCase,
    private val connectDevice: ConnectDeviceUseCase,
    private val syncWithGlucometer: SyncWithGlucometerUseCase,
    private val updateUserInfo: UpdateUserInfoUseCase,
    private val bus: RxBus,
    private val analytics: Analytics
) : BaseViewModel<ConnectingViewState>() {
    override fun createInitState(): ConnectingViewState =
        ConnectingViewState(
            stageType = ConnectingStageType.Connecting,
            isOnBoarding = false,
            pinCode = "",
            glucometerName = "",
            connectDevice = null
        )

    internal val appTopBar: BaseAppTopBarWidgetModel = BaseAppTopBarWidgetModel()
    val connectByPinButton: DownButtonWidgetModel = DownButtonWidgetModel()
    val connectRepeatButton: DownButtonWidgetModel = DownButtonWidgetModel()
    val syncRepeatButton: DownButtonWidgetModel = DownButtonWidgetModel()
    val searchRepeatButton: DownButtonWidgetModel = DownButtonWidgetModel()
    val completeButton: DownButtonWidgetModel = DownButtonWidgetModel()

    val exitDialogFromConnecting = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { exitFromScreen() }
    )
    val exitDialogFromSync = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { exitFromScreen() }
    )

    private var connectJob: Job? = null
    private var syncJob: Job? = null

    override val widgets = listOf(
        appTopBar,
        connectByPinButton,
        connectRepeatButton,
        syncRepeatButton,
        searchRepeatButton,
        completeButton
    ).actionObserve()

    override fun handleUserAction(action: Action) {
        when (action) {
            is ConnectAction.NeedHelp -> sendEvent(ConnectMainEvent.ShowSheet())
            is ConnectAction.CloseHelp -> sendEvent(ConnectMainEvent.HideSheet())
            is AppAction.BackPressure -> backClick()
            is ConnectAction.ConnectByPin -> connectByPin()
            is ConnectAction.Complete -> completeConnect()
        }
    }

    override fun reduceStateByAction(
        currentState: ConnectingViewState,
        action: Action
    ): ConnectingViewState = run {
        when (action) {
            is ConnectAction.RepeatConnect -> repeatConnectDevice(currentState)
            is ConnectAction.RepeatSync -> repeatSyncDevice(currentState)
            is ConnectAction.RepeatSearch -> repeatConnectDevice(currentState)
            else -> currentState
        }
    }

    private fun completeConnect() {
        updateUserInfo.execute(UpdateUserInfoUseCase.Params(UserInfo(isFirstSync = true)))
            .subscribe(
                {
                    bus.event(Events.DeviceChanged)
                    if (state.value.isOnBoarding) {
                        router.newRootScreen(Screens.HomeFlow)
                    } else {
                        router.backTo(Screens.Devices)
                    }
                },
                { handleError(it) }
            )
    }

    private fun repeatSyncDevice(currentState: ConnectingViewState) = run {
        syncDevice()
        currentState.copy(stageType = ConnectingStageType.Sync)
    }

    private fun repeatConnectDevice(currentState: ConnectingViewState) = run {
        connectDevice()
        currentState.copy(stageType = ConnectingStageType.Connecting)
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

    override fun handleFragmentArguments(arguments: Bundle) {
        reduceState {
            state.value.copy(
                isOnBoarding = arguments.getBoolean(IS_ON_BOARDING_ARGUMENT_NAME),
                pinCode = arguments.getString(PIN_ARGUMENT_NAME).orEmpty(),
                glucometerName = arguments.getString(GLUCOMETER_NAME_ARGUMENT_NAME).orEmpty()
            )
        }
        connectDevice()
    }

    override fun backClick() {
        when (state.value.stageType) {
            ConnectingStageType.Connecting -> exitDialogFromConnecting.dialogOpen()
            ConnectingStageType.Sync -> exitDialogFromSync.dialogOpen()
            else -> super.backClick()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun connectDevice() {
        connectJob?.cancel()
        connectJob = launch {
            findGlucometers.execute()
                .timeout(CONNECT_DEVICE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .doOnError { handleConnectError(it) }
                .asFlow()
                .cancellable()
                .map { devices -> devices.filter { it.name == state.value.glucometerName } }
                .filter { it.isNotEmpty() }
                .map { it.first() }
                .map {
                    reduceState { state.value.copy(connectDevice = it) }
                    ConnectDeviceUseCase.Params(
                        device = it,
                        pinCode = state.value.pinCode
                    )
                }
                .flatMapLatest {
                    connectDevice.execute(it)
                        .doOnComplete {
                            reduceState { state.value.copy(stageType = ConnectingStageType.Sync) }
                            syncDevice()
                            cancel()
                        }
                        .toObservable<Unit>()
                        .asFlow()
                }
                .catch { handleConnectError(it) }
                .collect()
        }
    }

    private fun syncDevice() {
        syncJob?.cancel()
        connectJob?.cancel()
        syncJob = launch {
            syncWithGlucometer.execute(SyncWithGlucometerUseCase.Params(state.value.connectDevice))
                .doOnComplete {
                    reduceState { state.value.copy(stageType = ConnectingStageType.Complete) }
                }
                .asFlow()
                .catch { handleSyncError(it) }
                .collect()
        }
    }

    private fun handleConnectError(error: Throwable) {
        reduceState { state.value.copy(stageType = ConnectingStageType.ErrorConnect) }
    }

    private fun handleSyncError(error: Throwable) {
        reduceState { state.value.copy(stageType = ConnectingStageType.ErrorSync) }
    }

    private fun exitFromScreen() {
        connectJob?.cancel()
        syncJob?.cancel()
        router.exit()
    }
}
