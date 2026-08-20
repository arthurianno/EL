package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.common.errors.BluetoothNotEnabledErrorVariantA
import com.elta.android.common.errors.LocationNotEnabledErrorVariantA
import com.elta.android.common.errors.LocationPermissionNotGrantedErrorVariantA
import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCaseVariantA
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCaseVariantA
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCaseVariantA
import com.elta.android.domain.features.devices.model.matchesTargetGlucometerName
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.analytics.Analytics
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.SynchronizedStatusParam
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.features.sync.connect.GLUCOMETER_NAME_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.PIN_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectMainEvent
import com.elta.android.presentation.features.sync.connect.model.ConnectingViewStateVariantA
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingStageType
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
import kotlinx.coroutines.rx2.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val CONNECT_DEVICE_TIMEOUT_SEC = 60L
// fixme Variant A : improved_enabling_location

class ConnectingViewModelVariantA @Inject constructor(
    private val findGlucometers: FindGlucometersUseCaseVariantA,
    private val connectDevice: AddNewDeviceUseCaseVariantA,
    private val syncWithGlucometer: SyncWithGlucometerUseCaseVariantA,
    private val updateUserInfo: UpdateUserInfoUseCase,
    private val bus: RxBus,
    private val analytics: Analytics,
    private val appMetric: AppMetricTracker
) : BaseViewModel<ConnectingViewStateVariantA>() {
    override fun createInitState(): ConnectingViewStateVariantA =
        ConnectingViewStateVariantA(
            stageType = ConnectingStageType.Connecting,
            isOnBoarding = false,
            pinCode = "",
            glucometerName = "",
            connectDevice = null,
            requestBluetoothActivation = false
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

    init {
        launch {
            state
                .map { it.stageType }
                .filter {
                    it == ConnectingStageType.Connecting ||
                            it == ConnectingStageType.Sync ||
                            it == ConnectingStageType.Complete ||
                            it == ConnectingStageType.ErrorSync
                }
                .collect { stageType ->
                    val eventName = when (stageType) {
                        ConnectingStageType.Connecting -> AppMetricEvent.ConnectionToDeviceScreen
                        ConnectingStageType.Sync -> AppMetricEvent.DeviceConnectedScreen
                        ConnectingStageType.Complete ->
                            AppMetricEvent.DeviceSynchronizedScreen(SynchronizedStatusParam.SUCCESS)

                        ConnectingStageType.ErrorSync ->
                            AppMetricEvent.DeviceSynchronizedScreen(SynchronizedStatusParam.ERROR)

                        else -> null
                    }

                    eventName?.let { appMetric.trackEvent(it) }
                }
        }
    }

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
        currentState: ConnectingViewStateVariantA,
        action: Action
    ): ConnectingViewStateVariantA = run {
        when (action) {
            is ConnectAction.RepeatConnect -> repeatConnectDevice(currentState)
            is ConnectAction.RepeatSync -> repeatSyncDevice(currentState)
            is ConnectAction.RepeatSearch -> repeatConnectDevice(currentState)
            is ConnectAction.ScannerError -> scannerError(currentState)
            else -> currentState
        }
    }

    private fun completeConnect() {
        launch {
            try {
                updateUserInfo.execute(UpdateUserInfoUseCase.Params(UserInfo(isFirstSync = true)))
                    .await()

                bus.event(Events.DeviceChanged)
                bus.event(Events.EventsChanged(true))

                if (state.value.isOnBoarding) router.newRootScreen(Screens.HomeFlowVariantA)
                else router.backTo(Screens.Devices)

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun repeatSyncDevice(currentState: ConnectingViewStateVariantA) = run {
        syncDevice()
        currentState.copy(stageType = ConnectingStageType.Sync)
    }

    private fun repeatConnectDevice(currentState: ConnectingViewStateVariantA) = run {
        connectDevice()
        currentState.copy(
            requestBluetoothActivation = false,
            stageType = ConnectingStageType.Connecting
        )
    }

    private fun scannerError(currentState: ConnectingViewStateVariantA) =
        currentState.copy(
            requestBluetoothActivation = false,
            stageType = ConnectingStageType.ErrorConnect
        )

    private fun connectByPin() {
        sendEvent(ConnectMainEvent.HideSheet())
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.PIN_CONNECTION))
        router.navigateTo(
            if (state.value.isOnBoarding) {
                Screens.FromOnBoardingConnectDeviceByPinVariantA
            } else {
                Screens.FromOtherConnectDeviceByPinVariantA
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
            ConnectingStageType.Complete -> router.newRootScreen(Screens.HomeFlowVariantA)
            else -> super.backClick()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun connectDevice() {
        connectJob?.cancel()
        connectJob = launch {
            findGlucometers.execute()
                .timeout(CONNECT_DEVICE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .asFlow()
                .cancellable()
                .map { devices ->
                    devices.filter {
                        matchesTargetGlucometerName(
                            deviceName = it.name,
                            targetName = state.value.glucometerName
                        )
                    }
                }
                .filter { it.isNotEmpty() }
                .map { it.first() }
                .map {
                    reduceState { state.value.copy(connectDevice = it) }
                    AddNewDeviceUseCaseVariantA.Params(
                        device = it,
                        pinCode = state.value.pinCode
                    )
                }
                .catch { handleNotFoundError(it) }
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
            syncWithGlucometer.execute(SyncWithGlucometerUseCaseVariantA.Params(state.value.connectDevice))
                .doOnComplete {
                    reduceState { state.value.copy(stageType = ConnectingStageType.Complete) }
                }
                .asFlow()
                .catch { handleSyncError(it) }
                .collect()
        }
    }

    private fun handleNotFoundError(error: Throwable) {
        if (handleLocationError(error)) return

        val newState = when (error) {
            BluetoothNotEnabledErrorVariantA -> state.value.copy(requestBluetoothActivation = true)
            else -> state.value.copy(stageType = ConnectingStageType.DeviceNotFound)
        }
        reduceState { newState }
    }

    private fun handleConnectError(error: Throwable) {
        if (handleLocationError(error)) return

        val newState = when (error) {
            BluetoothNotEnabledErrorVariantA -> state.value.copy(requestBluetoothActivation = true)
            else -> state.value.copy(stageType = ConnectingStageType.ErrorConnect)
        }
        reduceState { newState }
    }

    private fun handleSyncError(error: Throwable) {
        if (handleLocationError(error)) return

        reduceState { state.value.copy(stageType = ConnectingStageType.ErrorSync) }
    }

    private fun handleLocationError(error: Throwable): Boolean =
        when (error) {
            LocationPermissionNotGrantedErrorVariantA -> {
                reduceState {
                    state.value.copy(
                        stageType = ConnectingStageType.Connecting,
                        requestBluetoothActivation = false
                    )
                }
                sendEvent(PermissionEvent.RequestPermissions)
                true
            }

            LocationNotEnabledErrorVariantA -> {
                reduceState {
                    state.value.copy(
                        stageType = ConnectingStageType.Connecting,
                        requestBluetoothActivation = false
                    )
                }
                sendEvent(PermissionEvent.RequestEnableLocation)
                true
            }

            else -> false
        }

    private fun exitFromScreen() {
        connectJob?.cancel()
        syncJob?.cancel()
        router.exit()
    }
}
