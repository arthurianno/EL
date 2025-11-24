package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Bundle
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.diary.home.interactor.GetLocationNeededUseCase
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
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.features.sync.connect.GLUCOMETER_NAME_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.PIN_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingStageType
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingViewAction
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingViewEvent
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingViewState
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

class ConnectingViewModel @Inject constructor(
    private val findGlucometers: FindGlucometersUseCase,
    private val connectDevice: AddNewDeviceUseCase,
    private val syncWithGlucometer: SyncWithGlucometerUseCase,
    private val getLocationNeededUseCase: GetLocationNeededUseCase,
    private val updateUserInfo: UpdateUserInfoUseCase,
    private val bus: RxBus,
    private val analytics: Analytics,
    private val appMetric: AppMetricTracker
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

    val locationPermissionDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(ConnectingViewEvent.OpenSettings) }
    )

    val warningNeedLocation = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = {
            needEnableLocation = true
            sendEvent(ConnectingViewEvent.Location.RequestPermission)
        }
    )

    private var connectJob: Job? = null
    private var syncJob: Job? = null
    private var attempts: Int = 0
    private var needEnableLocation: Boolean = false
    private var deviceConnected: Boolean = false

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
            needEnableLocation = getLocationNeededUseCase.execute().await()
        }
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
            is AppAction.BackPressure -> backClick()
            is ConnectingViewAction.OpenHelp -> sendEvent(ConnectingViewEvent.ShowSheet)
            is ConnectingViewAction.CloseHelp -> sendEvent(ConnectingViewEvent.HideSheet)
            is ConnectingViewAction.OnConnectClick -> connectByPin()
            is ConnectingViewAction.ClickRepeatSyncButton -> repeatSyncDevice()
            is ConnectingViewAction.ClickCompleteButton -> completeConnect()
            is ConnectingViewAction.Location.AllowPermission -> sendEvent(ConnectingViewEvent.Location.Enable)
            is ConnectingViewAction.Location.ShowPermissionRationale -> locationPermissionDialog.dialogOpen()
            is ConnectingViewAction.ClickRepeatButton,
            is ConnectingViewAction.ClickSearchButton -> repeatConnectDevice()

            is ConnectingViewAction.Bluetooth.Enable,
            is ConnectingViewAction.Location.Enable -> {
                if (deviceConnected) repeatSyncDevice()
                else repeatConnectDevice()
            }
        }
    }

    override fun reduceStateByAction(
        currentState: ConnectingViewState,
        action: Action
    ): ConnectingViewState = run {
        when (action) {
            is ConnectingViewAction.Bluetooth.Reject -> currentState.copy(
                stageType = ConnectingStageType.ErrorConnect
            )

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
                if (state.value.isOnBoarding) router.newRootScreen(Screens.HomeFlow)
                else router.backTo(Screens.Devices)

            } catch (e: Exception) {
                handleError(e)
            }
        }
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
            ConnectingStageType.Complete -> router.newRootScreen(Screens.HomeFlow)
            else -> super.backClick()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun connectDevice() {
        val params = FindGlucometersUseCase.Params(
            isLocationNeeded = needEnableLocation,
            targetGlucometerName = state.value.glucometerName)
        connectJob?.cancel()
        connectJob = launch {
            findGlucometers.execute(params)
                .timeout(CONNECT_DEVICE_TIMEOUT_SEC, TimeUnit.SECONDS)
                .asFlow()
                .cancellable()
                .map { devices ->
                    devices.filter { it.name == state.value.glucometerName }
                }
                .filter {
                    it.isNotEmpty()
                }
                .map { it.first() }
                .map {
                    reduceState { state.value.copy(connectDevice = it) }
                    AddNewDeviceUseCase.Params(
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
                .catch {
                    reduceState { state.value.copy(stageType = ConnectingStageType.ErrorConnect) }
                    cancel()
                }
                .collect()
        }
    }

    private fun repeatSyncDevice() {
        reduceState { state.value.copy(stageType = ConnectingStageType.Sync) }
        syncDevice()
    }

    private fun repeatConnectDevice() {
        reduceState { state.value.copy(stageType = ConnectingStageType.Connecting) }
        connectDevice()
    }

    private fun connectByPin() {
        sendEvent(ConnectingViewEvent.HideSheet)
        analytics.trackEvent(AnalyticsEvent(AnalyticsEventType.PIN_CONNECTION))
        router.navigateTo(
            if (state.value.isOnBoarding) {
                Screens.FromOnBoardingConnectDeviceByPin
            } else {
                Screens.FromOtherConnectDeviceByPin
            }
        )
    }

    private fun syncDevice() {
        deviceConnected = true
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

    private fun handleNotFoundError(error: Throwable) {
        when {
            error is BluetoothNotEnabledError -> sendEvent(ConnectingViewEvent.EnableBluetooth)
            attempts >= 1 && !needEnableLocation -> showNeedLocationDialog()
            needEnableLocation -> handleLocationError(error)
            else -> attempts += 1
        }

        reduceState { state.value.copy(stageType = ConnectingStageType.DeviceNotFound) }
    }

    private fun handleLocationError(error: Throwable) {
        when (error) {
            LocationNotEnabledError -> sendEvent(ConnectingViewEvent.Location.Enable)
            LocationPermissionNotGrantedError -> sendEvent(ConnectingViewEvent.Location.RequestPermission)
            else -> reduceState { state.value.copy(stageType = ConnectingStageType.ErrorConnect) }
        }
    }

    private fun handleSyncError(error: Throwable) {
        when (error) {
            BluetoothNotEnabledError -> sendEvent(ConnectingViewEvent.EnableBluetooth)
            LocationNotEnabledError -> sendEvent(ConnectingViewEvent.Location.Enable)
        }
        reduceState { state.value.copy(stageType = ConnectingStageType.ErrorSync) }
    }

    private fun exitFromScreen() {
        connectJob?.cancel()
        syncJob?.cancel()
        router.backTo(Screens.ScannerDmcScreen(state.value.isOnBoarding))
    }

    private fun showNeedLocationDialog() {
        warningNeedLocation.dialogOpen()
    }
}
