package com.elta.android.presentation.features.sync.connect.base.pm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.elta.android.common.constants.GLUCOMETER_MODEL
import com.elta.android.common.errors.BluetoothNotEnabledErrorVariantA
import com.elta.android.common.errors.BluetoothScannerError
import com.elta.android.common.errors.CommandError
import com.elta.android.common.errors.GlucometerAlreadyConnectedError
import com.elta.android.common.errors.GlucometerConnectionException
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerPinIncorrect
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.LocationNotEnabledErrorVariantA
import com.elta.android.common.errors.LocationPermissionNotGrantedErrorVariantA
import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCaseVariantA
import com.elta.android.domain.features.devices.interactor.CheckConnectedDevicesUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCaseVariantA
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCaseVariantA
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getBluetoothPermissionGrantedMetricName
import com.elta.android.presentation.analytic.getLocationPermissionGrantedMetricName
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.AlertResultParam
import com.elta.android.presentation.analytic.model.appmetric.params.SynchronizedStatusParam
import com.elta.android.presentation.analytic.model.appmetric.params.TurningResultParam
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.snackBarControl
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.core.ui.snackbarview.SnackBarData
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.elta.android.presentation.features.sync.control.bluetoothControl
import com.elta.android.presentation.messages.SnackBarMessageData
import com.nullgr.core.rx.bindProgress
import io.reactivex.Observable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import java.util.concurrent.TimeoutException

abstract class ConnectDevicePmVariantA constructor(
    private val syncWithGlucometer: SyncWithGlucometerUseCaseVariantA,
    private val connectDevice: AddNewDeviceUseCaseVariantA,
    private val findGlucometers: FindGlucometersUseCaseVariantA,
    private val checkConnectedDevices: CheckConnectedDevicesUseCase,
    private val updateUserInfo: UpdateUserInfoUseCase,
    private val appMetric: AppMetricTracker,
    private val context: Context,
    private val getScreenConfigFromCacheUseCase: GetScreenConfigFromCache,
    services: ServiceFacade
) : BaseListPm(services) {

    // Переопределяем screenConfigKey и getScreenConfigUseCase для базовой поддержки
    override val screenConfigKey: String = "connect-device"
    override val getScreenConfigUseCase: GetScreenConfigFromCache = getScreenConfigFromCacheUseCase

    // State-ы для хранения конфигов 3 экранов
    val connectedScreenConfig = state<ScreenEntity?>()
    val connectedImageReady = state(true)

    val syncCompletedScreenConfig = state<ScreenEntity?>()
    val syncCompletedImageReady = state(true)

    val syncErrorScreenConfig = state<ScreenEntity?>()
    val syncErrorImageReady = state(true)

    val skipAction = action<Unit>()
    val backHandleAction = action<Unit>()

    val connectDeviceAction = action<Unit>()
    val connectDeviceEnabledState = state(false)

    val startScanAction = action<Unit>()
    val toAppAction = action<Unit>()
    private val locationNecessaryState = state(false)

    val openPinCodeDialogCommand = command<String>(bufferSize = 1)

    val connectState = state(ViewState.HOW_TO_CONNECT)

    val btControl = bluetoothControl()

    val retryPinControl = snackBarControl<SnackBarData>()
    val retryConnectControl = snackBarControl<SnackBarData>()

    private val scanResults = mutableSetOf<Glucometer>()
    private var glucometer: Glucometer? = null
    private var attemptsNumber: Int = 0

    val startSyncAction = action<Unit>()
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
    val showHomeButtonCommand = command<Unit>()
    val hideHomeButtonCommand = command<Unit>()

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

    private val incorrectPinCode: SnackBarData by lazy {
        SnackBarMessageData.WithButton(
            message = resources.getString(R.string.sync_connect_incorrect_pin_code),
            button = resources.getString(R.string.sync_connect_button_retry)
        )
    }

    private val connectError: SnackBarData by lazy {
        SnackBarMessageData.WithButton(
            message = resources.getString(R.string.sync_connect_connect_error),
            button = resources.getString(R.string.sync_connect_button_retry)
        )
    }

    private val showRetryPinAction = action<Unit>()
    private val showRetryConnectAction = action<Unit>()

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
            results["connected"]?.let { (config, imageReady) ->
                if (config != null) {
                    connectedScreenConfig.consumer.accept(config)
                }
                connectedImageReady.consumer.accept(imageReady)
            }

            // SYNC_COMPLETED screen config
            results["success"]?.let { (config, imageReady) ->
                if (config != null) {
                    syncCompletedScreenConfig.consumer.accept(config)
                }
                syncCompletedImageReady.consumer.accept(imageReady)
            }

            // SYNC_ERROR screen config
            results["failed"]?.let { (config, imageReady) ->
                if (config != null) {
                    syncErrorScreenConfig.consumer.accept(config)
                }
                syncErrorImageReady.consumer.accept(imageReady)
            }
        }

        bindActions()
        bindRetryActions()
        bindClicksAndEvents()
        bindAnalytics()
        bindPermissionAction()

        Observable.merge(
            btControl.bluetoothDeniedAction.observable,
            btControl.locationDeniedAction.observable
        )
            .subscribe { connectState.consumer.accept(ViewState.HOW_TO_CONNECT) }
            .untilDestroy()

        Observable.merge(
            btControl.bluetoothEnabledAction.observable,
            btControl.locationEnabledAction.observable
        )
            .doOnNext {
                locationNecessaryState.consumer.accept(true)
                startScanAction.consumer.accept(Unit)
            }
            .subscribe()
            .untilDestroy()


        openSettingsCloseAction.observable
            .subscribe { settingsIsVisible.accept(false) }
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is BluetoothNotEnabledErrorVariantA -> {
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlert)
                btControl.requestEnableBluetoothCommand.consumer.accept(Unit)
            }

            is LocationPermissionNotGrantedErrorVariantA -> requestMissingPermission()

            is GlucometerAlreadyConnectedError ->
                showDeviceAlreadyConnectedDialog()

            is LocationNotEnabledErrorVariantA -> {
                locationNecessaryState.consumer.accept(true)
                btControl.requestEnableLocationCommand.consumer.accept(Unit)
            }

            is CommandError, is BluetoothScannerError -> {
                connectState.consumer.accept(ViewState.SYNC_ERROR)
            }

            is GlucometerConnectionException -> {
                showRetryConnectAction.consumer.accept(Unit)
            }

            is GlucometerSyncError, is TimeoutException -> {
                val isTimeoutError = error is TimeoutException || error.cause is TimeoutException
                val isItemsEmpty = items.valueOrNull.isNullOrEmpty()
                if (
                    connectState.value == ViewState.SEARCH ||
                    connectState.value == ViewState.CONNECTED ||
                    connectState.value == ViewState.FOUND
                ) {
                    val syncState = when {
                        isTimeoutError || isItemsEmpty -> {
                            checkLocationNecessary()
                            ViewState.NOT_FOUND
                        }

                        else -> ViewState.SYNC_ERROR
                    }
                    connectState.consumer.accept(syncState)
                }
            }

            is GlucometerPinIncorrect -> showRetryPinAction.consumer.accept(Unit)
            is GlucometerOfflineError -> showRetryConnectAction.consumer.accept(Unit)
            else -> {
                connectState.consumer.accept(ViewState.SYNC_ERROR)
                super.handleError(error)
            }
        }
    }

    private fun bindActions() {
        bindStartScanAction()
        bindStartSyncAction()

        skipAction.observable
            .subscribe(::navigateToShopsFlow)
            .untilDestroy()

        connectDeviceAction.observable
            .skipWhileInProgress()
            .debounceAction()
            .map { CheckConnectedDevicesUseCase.Params(glucometer?.address) }
            .flatMap { params ->
                checkConnectedDevices.execute(params)
                    .bindProgress()
            }
            .doOnError(::handleError)
            .doOnNext { isAlreadyConnected ->
                when (isAlreadyConnected) {
                    true -> showDeviceAlreadyConnectedDialog()

                    else -> openPinCodeDialogCommand.consumer.accept(
                        glucometer?.name ?: GLUCOMETER_MODEL
                    )
                }
            }
            .subscribe()
            .untilDestroy()

        internalConnectDeviceAction.observable
            .skipWhileInProgress()
            .filter { glucometer != null && pinState.hasValue() }
            .map { AddNewDeviceUseCaseVariantA.Params(checkNotNull(glucometer), pinState.value) }
            .flatMapCompletable { params ->
                connectDevice.execute(params)
                    .bindProgress()
                    .doOnComplete {
                        startSyncAction.consumer.accept(Unit)
                        connectState.consumer.accept(ViewState.CONNECTED)
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        toAppAction.observable
            .flatMapSingle {
                updateUserInfo.execute(UpdateUserInfoUseCase.Params(UserInfo(isFirstSync = true)))
                    .toSingleDefault(Unit)
            }
            .subscribe(::navigateToApp)

        backHandleAction.observable
            .doOnNext(::handleBack)
            .subscribe()
            .untilDestroy()
    }

    private fun bindStartScanAction() {
        startScanAction.observable
            .flatMap {
                if (isLocationPermissionMissing()) {
                    locationNecessaryState.consumer.accept(true)
                    requestLocationPermission()
                    Observable.empty()
                } else {
                    findGlucometers.execute()
                        .takeUntil(backHandleAction.observable)
                        .doOnSubscribe {
                            connectState.consumer.accept(ViewState.SEARCH)
                        }
                        .doOnNext(::handleSearchResults)
                        .doOnError(::handleError)
                }
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindStartSyncAction() {
        startSyncAction.observable
            .doOnNext { connectState.consumer.accept(ViewState.CONNECTED) }
            .skipWhileInProgress(syncProgressState.observable)
            .filter { glucometer != null }
            .map { SyncWithGlucometerUseCaseVariantA.Params(glucometer) }
            .flatMap { params ->
                syncWithGlucometer.execute(params)
                    .bindProgress(syncProgressState.consumer)
                    .doOnNext { result ->
                        if (result.count > 0) bus.event(Events.EventsChanged(true))
                    }
                    .doOnComplete { connectState.consumer.accept(ViewState.SYNC_COMPLETED) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindRetryActions() {

        showRetryPinAction.observable
            .switchMapMaybe {
                retryPinControl.showForResult(incorrectPinCode)
            }
            .subscribe(connectDeviceAction.consumer)
            .untilDestroy()

        showRetryConnectAction.observable
            .switchMapMaybe {
                retryConnectControl.showForResult(connectError)
            }
            .subscribe(internalConnectDeviceAction.consumer)
            .untilDestroy()
    }

    private fun bindClicksAndEvents() {
        bus.events<Events.PinCodeEntered>()
            .map(Events.PinCodeEntered::pin)
            .doOnNext(pinState.consumer)
            .doOnNext { appMetric.trackEvent(AppMetricEvent.DeviceConnectClick) }
            .map { }
            .subscribe(internalConnectDeviceAction.consumer)
            .untilDestroy()

        bus.clicks<Clicks.DeviceClicked>()
            .doOnNext { appMetric.trackEvent(AppMetricEvent.SelectedDeviceConnectClick) }
            .doOnNext { click ->
                glucometer =
                    scanResults.firstOrNull { it.address == click.item.address && !click.item.isSelected }
                val prevItems = (items.value as List<*>).map { it as DeviceItem }
                val newItems = prevItems.mapIndexed { index, item ->
                    item.copy(
                        isSelected = item.address == click.item.address && !item.isSelected,
                        isLast = index == prevItems.size - 1
                    )
                }
                items.consumer.accept(newItems)
                connectDeviceEnabledState.consumer.accept(
                    isValidDeviceChoice(prevItems = prevItems, newItems = newItems)
                )
            }
            .subscribe()
            .untilDestroy()
    }

    private fun bindPermissionAction() {
        onLocationPermissionGrantedAction.observable
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.Permission.AlertClick.Location(AlertResultParam.ALLOW))
            }
            .doOnNext { startScanAction.consumer.accept(Unit) }
            .subscribe()
            .untilDestroy()

        receivedLocationPermissionGrantedAction.observable
            .doOnNext { appMetric.trackEvent(it.getLocationPermissionGrantedMetricName()) }
            .doOnNext { isGranted ->
                if (isGranted) startScanAction.consumer.accept(Unit)
                else connectState.consumer.accept(ViewState.HOW_TO_CONNECT)
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
            .doOnNext { startScanAction.consumer.accept(Unit) }
            .subscribe()
            .untilDestroy()

        receivedBluetoothPermissionGrantedAction.observable
            .doOnNext { appMetric.trackEvent(it.getBluetoothPermissionGrantedMetricName()) }
            .doOnNext { isGranted ->
                if (isGranted) startScanAction.consumer.accept(Unit)
                else connectState.consumer.accept(ViewState.HOW_TO_CONNECT)
            }
            .subscribe()
            .untilDestroy()

        showBluetoothPermissionRationaleAction.observable
            .doOnNext {
                appMetric.trackEvent(AppMetricEvent.Permission.AlertClick.Bluetooth(AlertResultParam.PROHIBIT))
            }
            .doOnNext { showSettingDialog(settingsBluetoothDialogData) }
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

    private fun requestMissingPermission() {
        when {
            isLocationPermissionMissing() -> requestLocationPermission()
            isBluetoothPermissionMissing() -> requestBluetoothPermission()
            else -> requestLocationPermission()
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

    private fun isLocationPermissionMissing(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED

    private fun isBluetoothPermissionMissing(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                        )

    private fun showDeviceAlreadyConnectedDialog() {
        deviceAlreadyConnectedDialog.showForResult(deviceAlreadyConnectedDialogData)
            .subscribe()
            .untilDestroy()
    }

    private fun handleBack(i: Unit) {
        when (connectState.valueOrNull) {
            ViewState.SYNC_COMPLETED,
            ViewState.SYNC_ERROR -> toAppAction.consumer.accept(i)

            ViewState.NOT_FOUND,
            ViewState.CONNECTED,
            ViewState.SEARCH -> connectState.consumer.accept(ViewState.HOW_TO_CONNECT)

            ViewState.FOUND -> {
                items.consumer.accept(emptyList())
                connectState.consumer.accept(ViewState.HOW_TO_CONNECT)
            }

            else -> router.exit()
        }
    }

    private fun isValidDeviceChoice(
        prevItems: List<DeviceItem>,
        newItems: List<DeviceItem>
    ) = glucometer != null ||
            isDevicesEquals(
                prevItems = prevItems,
                newItems = newItems
            )

    private fun isDevicesEquals(
        prevItems: List<DeviceItem>,
        newItems: List<DeviceItem>
    ) = prevItems.map { it.address to it.isSelected } ==
            newItems.map { it.address to it.isSelected }

    private fun navigateToShopsFlow(i: Unit) {
        // todo: SalepointHide
        // скрываем точки продаж пока пока не примем решение что с ними делать
//        router.newRootFlow(Screens.ShopsFlow)
    }

    private fun handleSearchResults(results: List<Glucometer>) {
        if (results.isNotEmpty()) {
            connectState.consumer.accept(ViewState.FOUND)
        }
        scanResults.clear()
        scanResults.addAll(results)
        items.consumer.accept(
            results.mapIndexed { index, meter ->
                mapToDeviceItem(meter, results.size, index)
            }
        )
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
            .filter { it == ViewState.SYNC_COMPLETED }
            .trackEvent(AnalyticsEventType.GLUCOMETER_SYNCH)
            .doOnNext(::handleHomeButton)
            .subscribe()
            .untilDestroy()

        connectState.observable
            .filter {
                it == ViewState.FOUND ||
                        it == ViewState.CONNECTED ||
                        it == ViewState.SYNC_COMPLETED ||
                        it == ViewState.SYNC_ERROR
            }
            .subscribe { state ->
                val eventName = when (state) {
                    ViewState.FOUND -> AppMetricEvent.DevicesFoundScreen
                    ViewState.CONNECTED -> AppMetricEvent.DeviceConnectedScreen
                    ViewState.SYNC_COMPLETED ->
                        AppMetricEvent.DeviceSynchronizedScreen(SynchronizedStatusParam.SUCCESS)

                    ViewState.SYNC_ERROR ->
                        AppMetricEvent.DeviceSynchronizedScreen(SynchronizedStatusParam.ERROR)

                    else -> null
                }
                eventName?.let { appMetric.trackEvent(it) }
            }

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

    private fun handleHomeButton(state: ViewState) {
        if (state == ViewState.SYNC_COMPLETED) hideHomeButtonCommand.consumer.accept(Unit)
        else showHomeButtonCommand.consumer.accept(Unit)
    }

    private fun showSettingDialog(dialogData: DialogData) {
        connectState.consumer.accept(ViewState.HOW_TO_CONNECT)
        settingsDialog.showForResult(dialogData)
            .map { it == DialogResult.POSITIVE }
            .subscribe(settingsIsVisible.consumer)
            .untilDestroy()
    }

    enum class ViewState {
        HOW_TO_CONNECT,
        SEARCH,
        FOUND,
        CONNECTED,
        SYNC_COMPLETED,
        SYNC_ERROR,
        NOT_FOUND
    }
}
