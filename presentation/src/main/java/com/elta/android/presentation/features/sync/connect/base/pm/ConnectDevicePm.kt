package com.elta.android.presentation.features.sync.connect.base.pm

import android.os.Build
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.BluetoothPermissionNotGrantedError
import com.elta.android.common.errors.BluetoothScannerError
import com.elta.android.common.errors.CommandError
import com.elta.android.common.errors.GlucometerAlreadyConnectedError
import com.elta.android.common.errors.GlucometerConnectionException
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerPinIncorrect
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCase
import com.elta.android.domain.features.devices.interactor.CheckConnectedDevicesUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.diary.home.interactor.GetLocationNeededUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getBluetoothPermissionGrantedMetricName
import com.elta.android.presentation.analytic.getLocationPermissionGrantedMetricName
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.AlertResultParam
import com.elta.android.presentation.analytic.model.appmetric.params.SynchronizedStatusParam
import com.elta.android.presentation.analytic.model.appmetric.params.TurningResultParam
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.sync.connect.pin.PinConnectionModel
import com.elta.android.presentation.features.sync.connect.pin.PinConnectionStage
import com.elta.android.presentation.features.sync.connect.pin.asGlucometerPin
import com.elta.android.presentation.features.sync.connect.pin.isGlucometerPinValid
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.elta.android.presentation.features.sync.control.bluetoothControl
import com.nullgr.core.rx.bindProgress
import io.reactivex.Observable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import java.util.concurrent.TimeoutException

abstract class ConnectDevicePm constructor(
    private val syncWithGlucometer: SyncWithGlucometerUseCase,
    private val connectDevice: AddNewDeviceUseCase,
    private val findGlucometers: FindGlucometersUseCase,
    private val checkConnectedDevices: CheckConnectedDevicesUseCase,
    private val getLocationNeededUseCase: GetLocationNeededUseCase,
    private val updateUserInfo: UpdateUserInfoUseCase,
    private val appMetric: AppMetricTracker,
    private val context: android.content.Context,
    private val getScreenConfigFromCacheUseCase: GetScreenConfigFromCache,
    services: ServiceFacade
) : BaseListPm(services), PinConnectionModel {

    override val screenConfigKey: String = "connect-device"
    override val getScreenConfigUseCase: GetScreenConfigFromCache = getScreenConfigFromCacheUseCase

    override val connectedScreenConfig = state<ScreenEntity?>()

    override val syncCompletedScreenConfig = state<ScreenEntity?>()

    override val syncErrorScreenConfig = state<ScreenEntity?>()

    override val backHandleAction = action<Unit>()

    override val connectDeviceAction = action<Unit>()
    override val connectDeviceEnabledState = state(false)

    override val startScanAction = action<Unit>()
    override val toAppAction = action<Unit>()
    private val locationNecessaryState = state(false)

    override val pinInputState = state("")
    override val pinIncorrectState = state(false)
    override val selectDeviceAction = action<DeviceItem>()
    override val pinChangedAction = action<String>()
    override val submitPinAction = action<Unit>()
    override val retryConnectAction = action<Unit>()

    override val connectState = state(PinConnectionStage.HOW_TO_CONNECT)

    val btControl = bluetoothControl()

    private var attemptsNumber: Int = 0
    private val scanResults = mutableSetOf<Glucometer>()
    private var glucometer: Glucometer? = null
    private var pendingPermissionStage: PinConnectionStage? = null

    override val startSyncAction = action<Unit>()
    private val syncProgressState = state(false)

    private val settingsLocationDialogData: DialogData by lazy {
        Dialogs.SettingsLocationDialogData(resources)
    }
    private val settingsBluetoothDialogData: DialogData by lazy {
        Dialogs.SettingsBluetoothDialogData(resources)
    }
    private val deviceAlreadyConnectedDialogData: DialogData by lazy {
        Dialogs.DeviceAlreadyConnectedDialogData(resources)
    }
    private val deviceNeedLocationDialogData: DialogData by lazy {
        Dialogs.DeviceNeedLocationDialogData(resources)
    }

    val settingsDialog = dialogControl<DialogData, DialogResult>()
    val settingsIsVisible = state(false)
    val openSettingsCloseAction = action<Unit>()

    val deviceAlreadyConnectedDialog = dialogControl<DialogData, DialogResult>()

    val deviceNeedLocationDialog = dialogControl<DialogData, DialogResult>()

    val checkLocationPermissionCommand = command<Unit>()
    val receivedLocationPermissionGrantedAction = action<Boolean>()
    val showLocationPermissionRationaleAction = action<Unit>()
    val onLocationPermissionGrantedAction = action<Unit>()

    val checkBluetoothPermissionCommand = command<Unit>()
    val receivedBluetoothPermissionGrantedAction = action<Boolean>()
    val showBluetoothPermissionRationaleAction = action<Unit>()
    val onBluetoothPermissionGrantedAction = action<Unit>()

    private val internalConnectDeviceAction = action<Unit>()
    private val pinState = state<String>()

    protected abstract fun navigateToApp(i: Unit)

    override fun onCreate() {
        super.onCreate()

        // Загружаем конфигурации для 3 экранов
        loadMultipleScreenConfigs(
            context = context,
            configs = mapOf(
                "connected" to "sync-screen",
                "success" to "successful-sync-screen",
                "failed" to "failed-sync-screen"
            )
        ) { results ->
            // CONNECTED screen config
            results["connected"]?.let { (config, _) ->
                if (config != null) {
                    connectedScreenConfig.consumer.accept(config)
                }
            }

            // SYNC_COMPLETED screen config
            results["success"]?.let { (config, _) ->
                if (config != null) {
                    syncCompletedScreenConfig.consumer.accept(config)
                }
            }

            // SYNC_ERROR screen config
            results["failed"]?.let { (config, _) ->
                if (config != null) {
                    syncErrorScreenConfig.consumer.accept(config)
                }
            }
        }

        bindActions()
        bindClicksAndEvents()
        bindAnalytics()
        bindPermissionAction()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .flatMapSingle {
                getLocationNeededUseCase.execute()
                    .doOnSuccess { locationNecessaryState.consumer.accept(it) }
            }
            .subscribe()
            .untilDestroy()

        Observable.merge(
            btControl.bluetoothDeniedAction.observable,
            btControl.locationDeniedAction.observable
        )
            .subscribe {
                pendingPermissionStage = null
                connectState.consumer.accept(PinConnectionStage.HOW_TO_CONNECT)
            }
            .untilDestroy()

        Observable.merge(
            btControl.bluetoothEnabledAction.observable,
            btControl.locationEnabledAction.observable
        )
            .doOnNext { resumeAfterPermission() }
            .subscribe()
            .untilDestroy()

        openSettingsCloseAction.observable
            .subscribe { settingsIsVisible.accept(false) }
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is BluetoothNotEnabledError -> {
                pendingPermissionStage = connectState.value
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlert)
                btControl.requestEnableBluetoothCommand.consumer.accept(Unit)
            }

            is LocationPermissionNotGrantedError -> {
                pendingPermissionStage = connectState.value
                requestLocationPermission()
            }

            is BluetoothPermissionNotGrantedError -> {
                pendingPermissionStage = connectState.value
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) requestBluetoothPermission()
                else requestLocationPermission()
            }

            is GlucometerAlreadyConnectedError ->
                showDeviceAlreadyConnectedDialog()

            is LocationNotEnabledError -> {
                pendingPermissionStage = connectState.value
                btControl.requestEnableLocationCommand.consumer.accept(Unit)
            }

            is GlucometerPinIncorrect -> {
                pinState.consumer.accept("")
                pinInputState.consumer.accept("")
                pinIncorrectState.consumer.accept(true)
                connectState.consumer.accept(PinConnectionStage.ENTER_PIN)
            }

            is CommandError, is BluetoothScannerError,
            is GlucometerConnectionException, is GlucometerSyncError,
            is GlucometerOfflineError, is TimeoutException -> {
                connectState.consumer.accept(connectState.value.operationError())
                if (connectState.value == PinConnectionStage.NOT_FOUND) checkLocationNecessary()
            }

            else -> {
                connectState.consumer.accept(connectState.value.operationError())
                super.handleError(error)
            }
        }
    }

    private fun bindActions() {
        bindStartScanAction()
        bindStartSyncAction()

        connectDeviceAction.observable
            .filter { connectState.value == PinConnectionStage.FOUND && glucometer != null }
            .skipWhileInProgress()
            .debounceAction()
            .map { CheckConnectedDevicesUseCase.Params(glucometer?.address) }
            .switchMap { params ->
                checkConnectedDevices.execute(params)
                    .bindProgress()
                    .doOnNext { isAlreadyConnected ->
                        if (isAlreadyConnected) {
                            showDeviceAlreadyConnectedDialog()
                        } else {
                            pinInputState.consumer.accept("")
                            pinIncorrectState.consumer.accept(false)
                            connectState.consumer.accept(PinConnectionStage.ENTER_PIN)
                        }
                    }
                    .doOnError(::handleError)
                    .takeUntil(backHandleAction.observable)
                    .onErrorResumeNext(Observable.empty())
            }
            .subscribe()
            .untilDestroy()

        internalConnectDeviceAction.observable
            .filter {
                glucometer != null && pinState.valueOrNull.orEmpty().isGlucometerPinValid() &&
                    connectState.value in setOf(PinConnectionStage.ENTER_PIN, PinConnectionStage.CONNECT_ERROR)
            }
            .flatMapCompletable {
                val params = AddNewDeviceUseCase.Params(checkNotNull(glucometer), pinState.value)
                connectDevice.execute(params)
                    .doOnSubscribe { connectState.consumer.accept(PinConnectionStage.CONNECTING) }
                    .doOnComplete { startSyncAction.consumer.accept(Unit) }
                    .doOnError(::handleError)
                    .takeUntil(backHandleAction.observable.firstOrError().ignoreElement())
                    .onErrorComplete()
            }
            .subscribe()
            .untilDestroy()

        toAppAction.observable
            .skipWhileInProgress()
            .flatMap {
                updateUserInfo.execute(UpdateUserInfoUseCase.Params(UserInfo(isFirstSync = true)))
                    .bindProgress()
                    .toSingleDefault(Unit)
                    .toObservable()
                    .doOnError { super.handleError(it) }
                    .onErrorResumeNext(Observable.empty())
            }
            .subscribe(::navigateToApp)
            .untilDestroy()

        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()
    }

    private fun bindStartScanAction() {
        startScanAction.observable
            .filter { connectState.value !in setOf(PinConnectionStage.CONNECTING, PinConnectionStage.CONNECTED) }
            .switchMap {
                findGlucometers.execute(FindGlucometersUseCase.Params(isLocationNeeded = locationNecessaryState.value))
                    .doOnSubscribe {
                        pendingPermissionStage = null
                        scanResults.clear()
                        glucometer = null
                        items.consumer.accept(emptyList())
                        connectDeviceEnabledState.consumer.accept(false)
                        pinState.consumer.accept("")
                        pinInputState.consumer.accept("")
                        pinIncorrectState.consumer.accept(false)
                        connectState.consumer.accept(PinConnectionStage.SEARCH)
                    }
                    .doOnNext(::handleSearchResults)
                    .doOnComplete {
                        if (connectState.value == PinConnectionStage.SEARCH) {
                            connectState.consumer.accept(PinConnectionStage.NOT_FOUND)
                            checkLocationNecessary()
                        }
                    }
                    .doOnError(::handleError)
                    .takeUntil(Observable.merge(backHandleAction.observable, connectDeviceAction.observable))
                    .onErrorResumeNext(Observable.empty())
            }
            .subscribe()
            .untilDestroy()
    }

    private fun bindStartSyncAction() {
        startSyncAction.observable
            .filter { glucometer != null }
            .skipWhileInProgress(syncProgressState.observable)
            .map { SyncWithGlucometerUseCase.Params(glucometer) }
            .switchMap { params ->
                syncWithGlucometer.execute(params)
                    .bindProgress(syncProgressState.consumer)
                    .doOnSubscribe { connectState.consumer.accept(PinConnectionStage.CONNECTED) }
                    .doOnNext { result ->
                        if (result.count > 0) bus.event(Events.EventsChanged(true))
                    }
                    .doOnComplete { connectState.consumer.accept(PinConnectionStage.SYNC_COMPLETED) }
                    .doOnError(::handleError)
                    .takeUntil(backHandleAction.observable)
                    .onErrorResumeNext(Observable.empty())
            }
            .subscribe()
            .untilDestroy()
    }

    private fun bindClicksAndEvents() {
        pinChangedAction.observable
            .filter { connectState.value == PinConnectionStage.ENTER_PIN }
            .subscribe {
                pinInputState.consumer.accept(it.asGlucometerPin())
                pinIncorrectState.consumer.accept(false)
            }
            .untilDestroy()

        submitPinAction.observable
            .filter { connectState.value == PinConnectionStage.ENTER_PIN && pinInputState.value.isGlucometerPinValid() }
            .subscribe {
                pinState.consumer.accept(pinInputState.value)
                appMetric.trackEvent(AppMetricEvent.DeviceConnectClick)
                internalConnectDeviceAction.consumer.accept(Unit)
            }
            .untilDestroy()

        retryConnectAction.observable
            .filter { connectState.value == PinConnectionStage.CONNECT_ERROR }
            .subscribe(internalConnectDeviceAction.consumer)
            .untilDestroy()

        selectDeviceAction.observable
            .filter { connectState.value == PinConnectionStage.FOUND }
            .subscribe { selected ->
                appMetric.trackEvent(AppMetricEvent.SelectedDeviceConnectClick)
                glucometer = scanResults.firstOrNull { it.address == selected.address && !selected.isSelected }
                val selectedAddress = glucometer?.address
                val updatedItems = items.valueOrNull.orEmpty().filterIsInstance<DeviceItem>().map {
                    it.copy(isSelected = it.address == selectedAddress)
                }
                items.consumer.accept(updatedItems)
                connectDeviceEnabledState.consumer.accept(glucometer != null)
            }
            .untilDestroy()
    }

    private fun bindPermissionAction() {
        onLocationPermissionGrantedAction.observable
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.Permission.AlertClick.Location(AlertResultParam.ALLOW))
            }
            .doOnNext { resumeAfterPermission() }
            .subscribe()
            .untilDestroy()

        receivedLocationPermissionGrantedAction.observable
            .doOnNext { appMetric.trackEvent(it.getLocationPermissionGrantedMetricName()) }
            .doOnNext { isGranted ->
                if (isGranted) resumeAfterPermission()
                else {
                    pendingPermissionStage = null
                    connectState.consumer.accept(PinConnectionStage.HOW_TO_CONNECT)
                }
            }
            .subscribe()
            .untilDestroy()

        showLocationPermissionRationaleAction.observable
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.Permission.AlertClick.Location(AlertResultParam.PROHIBIT))
            }
            .doOnNext { showSettingDialog(settingsLocationDialogData) }
            .subscribe()
            .untilDestroy()

        onBluetoothPermissionGrantedAction.observable
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.Permission.AlertClick.Bluetooth(AlertResultParam.ALLOW))
            }
            .doOnNext { resumeAfterPermission() }
            .subscribe()
            .untilDestroy()

        showBluetoothPermissionRationaleAction.observable
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.Permission.AlertClick.Bluetooth(AlertResultParam.PROHIBIT))
            }
            .doOnNext { showSettingDialog(settingsBluetoothDialogData) }
            .subscribe()
            .untilDestroy()

        receivedBluetoothPermissionGrantedAction.observable
            .doOnNext { appMetric.trackEvent(it.getBluetoothPermissionGrantedMetricName()) }
            .doOnNext { isGranted ->
                if (isGranted) resumeAfterPermission()
                else {
                    pendingPermissionStage = null
                    connectState.consumer.accept(PinConnectionStage.HOW_TO_CONNECT)
                }
            }
            .subscribe()
            .untilDestroy()
    }

    private fun checkLocationNecessary() {
        if (attemptsNumber >= 1 && !locationNecessaryState.value) {
            deviceNeedLocationDialog.showForResult(deviceNeedLocationDialogData)
                .filter { it == DialogResult.POSITIVE }
                .subscribe {
                    locationNecessaryState.consumer.accept(true)
                    startScanAction.consumer.accept(Unit)
                }
                .untilDestroy()
        } else {
            attemptsNumber += 1
        }
    }

    private fun requestBluetoothPermission() {
        checkBluetoothPermissionCommand.consumer.accept(Unit)
        appMetric.trackEvent(AppMetricEvent.Permission.Alert.Bluetooth)
    }

    private fun requestLocationPermission() {
        checkLocationPermissionCommand.consumer.accept(Unit)
        appMetric.trackEvent(AppMetricEvent.Permission.Alert.Location)
    }

    private fun showDeviceAlreadyConnectedDialog() {
        deviceAlreadyConnectedDialog.showForResult(deviceAlreadyConnectedDialogData)
            .subscribe()
            .untilDestroy()
    }

    private fun resumeAfterPermission() {
        val pending = pendingPermissionStage ?: return
        pendingPermissionStage = null
        when (pending) {
            PinConnectionStage.CONNECTING -> {
                connectState.consumer.accept(PinConnectionStage.CONNECT_ERROR)
                internalConnectDeviceAction.consumer.accept(Unit)
            }
            PinConnectionStage.CONNECTED -> startSyncAction.consumer.accept(Unit)
            else -> startScanAction.consumer.accept(Unit)
        }
    }

    private fun handleBack(i: Unit) {
        pendingPermissionStage = null
        when (connectState.value) {
            PinConnectionStage.SYNC_COMPLETED,
            PinConnectionStage.SYNC_ERROR -> toAppAction.consumer.accept(i)

            PinConnectionStage.ENTER_PIN,
            PinConnectionStage.CONNECT_ERROR -> connectState.consumer.accept(PinConnectionStage.FOUND)

            PinConnectionStage.NOT_FOUND,
            PinConnectionStage.CONNECTING,
            PinConnectionStage.CONNECTED,
            PinConnectionStage.SEARCH,
            PinConnectionStage.FOUND -> {
                items.consumer.accept(emptyList())
                connectDeviceEnabledState.consumer.accept(false)
                glucometer = null
                connectState.consumer.accept(PinConnectionStage.HOW_TO_CONNECT)
            }

            PinConnectionStage.HOW_TO_CONNECT -> router.exit()
        }
    }

    private fun handleSearchResults(results: List<Glucometer>) {
        if (!connectState.value.isSearching) return
        scanResults.clear()
        scanResults.addAll(results.distinctBy { it.address })
        glucometer = scanResults.firstOrNull { it.address == glucometer?.address }
        items.consumer.accept(
            scanResults.mapIndexed { index, meter -> mapToDeviceItem(meter, scanResults.size, index) }
        )
        connectDeviceEnabledState.consumer.accept(glucometer != null)
        connectState.consumer.accept(if (scanResults.isEmpty()) PinConnectionStage.SEARCH else PinConnectionStage.FOUND)
    }

    private fun mapToDeviceItem(meter: Glucometer, size: Int, index: Int): DeviceItem =
        DeviceItem(
            id = meter.id,
            name = meter.name ?: "Unknown device",
            address = meter.address,
            isSelected = meter.address == glucometer?.address,
            isLast = index == size - 1
        )

    private fun bindAnalytics() {

        connectState.observable
            .filter { it == PinConnectionStage.SYNC_COMPLETED }
            .trackEvent(AnalyticsEventType.GLUCOMETER_SYNCH)
            .subscribe()
            .untilDestroy()

        connectState.observable
            .filter {
                it == PinConnectionStage.FOUND ||
                        it == PinConnectionStage.CONNECTED ||
                        it == PinConnectionStage.SYNC_COMPLETED ||
                        it == PinConnectionStage.SYNC_ERROR
            }
            .subscribe { state ->
                val eventName = when (state) {
                    PinConnectionStage.FOUND -> AppMetricEvent.DevicesFoundScreen
                    PinConnectionStage.CONNECTED -> AppMetricEvent.DeviceConnectedScreen
                    PinConnectionStage.SYNC_COMPLETED ->
                        AppMetricEvent.DeviceSynchronizedScreen(SynchronizedStatusParam.SUCCESS)

                    PinConnectionStage.SYNC_ERROR ->
                        AppMetricEvent.DeviceSynchronizedScreen(SynchronizedStatusParam.ERROR)

                    else -> null
                }
                eventName?.let { appMetric.trackEvent(it) }
            }
            .untilDestroy()

        btControl.bluetoothDeniedAction.observable
            .subscribe {
                appMetric.trackEvent(
                    AppMetricEvent.BluetoothTurningAlertClick(
                        TurningResultParam.REJECT
                    )
                )
            }
            .untilDestroy()

        btControl.bluetoothEnabledAction.observable
            .subscribe {
                appMetric.trackEvent(
                    AppMetricEvent.BluetoothTurningAlertClick(
                        TurningResultParam.ALLOW
                    )
                )
            }
            .untilDestroy()
    }

    private fun showSettingDialog(dialogData: DialogData) {
        pendingPermissionStage = null
        connectState.consumer.accept(PinConnectionStage.HOW_TO_CONNECT)
        settingsDialog.showForResult(dialogData)
            .map { it == DialogResult.POSITIVE }
            .subscribe(settingsIsVisible.consumer)
            .untilDestroy()
    }

}
