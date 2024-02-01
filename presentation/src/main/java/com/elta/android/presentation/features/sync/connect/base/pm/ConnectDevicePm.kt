package com.elta.android.presentation.features.sync.connect.base.pm

import com.elta.android.common.constants.GLUCOMETER_MODEL
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerPinIncorrect
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.AddNewDeviceUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.model.AnalyticsEventType
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

abstract class ConnectDevicePm constructor(
    private val syncWithGlucometer: SyncWithGlucometerUseCase,
    private val connectDevice: AddNewDeviceUseCase,
    private val findGlucometers: FindGlucometersUseCase,
    private val updateUserInfo: UpdateUserInfoUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val skipAction = action<Unit>()
    val backHandleAction = action<Unit>()

    val connectDeviceAction = action<Unit>()
    val connectDeviceEnabledState = state(false)

    val startScanAction = action<Unit>()
    val toAppAction = action<Unit>()

    val openPinCodeDialogCommand = command<String>(bufferSize = 1)

    val connectState = state(ViewState.HOW_TO_CONNECT)

    val btControl = bluetoothControl()

    val retryPinControl = snackBarControl<SnackBarData>()
    val retryConnectControl = snackBarControl<SnackBarData>()

    private val scanResults = mutableSetOf<Glucometer>()
    private var glucometer: Glucometer? = null

    val startSyncAction = action<Unit>()
    private val syncProgressState = state(false)

    val requestBluetoothPermissionCommand = command<Unit>()

    val settingsDialog = dialogControl<DialogData, DialogResult>()
    private val settingsDialogData: DialogData by lazy { Dialogs.SettingsDialogData(resources) }
    val settingsIsVisible = state(false)
    val openSettingsCloseAction = action<Unit>()
    val showHomeButtonCommand = command<Unit>()
    val hideHomeButtonCommand = command<Unit>()

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
        bindActions()
        bindRetryActions()
        bindClicksAndEvents()
        bindAnalytics()

        btControl.bluetoothDeniedAction.observable
            .subscribe { connectState.consumer.accept(ViewState.HOW_TO_CONNECT) }
            .untilDestroy()

        Observable.merge(
            btControl.bluetoothEnabledAction.observable,
            btControl.locationEnabledAction.observable
        )
            .subscribe(startScanAction.consumer)
            .untilDestroy()

        btControl.locationPermissionsGrantedAction.observable
            .subscribe { permission ->
                when {
                    permission.granted -> startScanAction.consumer.accept(Unit)
                    !permission.granted && !permission.shouldShowRequestPermissionRationale -> {
                        connectState.consumer.accept(ViewState.HOW_TO_CONNECT)
                        settingsDialog.showForResult(settingsDialogData)
                            .map { it == DialogResult.POSITIVE }
                            .subscribe(settingsIsVisible.consumer)
                    }

                    !permission.granted -> connectState.consumer.accept(ViewState.HOW_TO_CONNECT)
                }
            }
            .untilDestroy()

        openSettingsCloseAction.observable
            .subscribe { settingsIsVisible.accept(false) }
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is BluetoothNotEnabledError ->
                btControl.requestEnableBluetoothCommand.consumer.accept(Unit)

            is LocationPermissionNotGrantedError ->
                // TODO необходимо дописать логику как с запросом блютуза. Менять флоу экрана
                // а также необходимо делать повтор на поиск устройств если приняли разрешение
                requestBluetoothPermissionCommand.consumer.accept(Unit)

            is LocationNotEnabledError ->
                btControl.requestEnableLocationCommand.consumer.accept(Unit)

            is TimeoutException -> {
                val syncState = if (items.valueOrNull.isNullOrEmpty()) {
                    ViewState.NOT_FOUND
                } else {
                    ViewState.SYNC_ERROR
                }
                connectState.consumer.accept(syncState)
            }

            is GlucometerPinIncorrect -> showRetryPinAction.consumer.accept(Unit)
            is GlucometerSyncError -> connectState.consumer.accept(ViewState.SYNC_ERROR)
            is GlucometerOfflineError -> showRetryConnectAction.consumer.accept(Unit)
            else -> super.handleError(error)
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
            .map { glucometer?.name ?: GLUCOMETER_MODEL }
            .subscribe(openPinCodeDialogCommand.consumer)
            .untilDestroy()

        internalConnectDeviceAction.observable
            .skipWhileInProgress()
            .filter { glucometer != null && pinState.hasValue() }
            .map { AddNewDeviceUseCase.Params(checkNotNull(glucometer), pinState.value) }
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
                findGlucometers.execute()
                    .doOnSubscribe {
                        connectState.consumer.accept(ViewState.SEARCH)
                    }
                    .doOnNext(::handleSearchResults)
                    .doOnError(::handleError)
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
            .map { SyncWithGlucometerUseCase.Params(glucometer) }
            .flatMap { params ->
                syncWithGlucometer.execute(params)
                    .bindProgress(syncProgressState.consumer)
                    .doOnNext { events ->
                        if (events > 0) bus.event(Events.EventsChanged(true))
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
            .map { Unit }
            .subscribe(internalConnectDeviceAction.consumer)
            .untilDestroy()

        bus.clicks<Clicks.DeviceClicked>()
            .doOnNext { click ->
                glucometer = scanResults.firstOrNull { it.address == click.item.address }
                val prevItems = (items.value as List<*>).map { it as DeviceItem }
                val newItems = prevItems.mapIndexed { index, item ->
                    item.copy(
                        isSelected = item.address == click.item.address,
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

    private fun handleBack(i: Unit) {
        if (connectState.valueOrNull == ViewState.SYNC_COMPLETED) toAppAction.consumer.accept(Unit)
        else router.exit()
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
    ) = prevItems.map { it.address } == newItems.map { it.address }

    private fun navigateToShopsFlow(i: Unit) {
        router.newRootFlow(Screens.ShopsFlow)
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
    }

    private fun handleHomeButton(state: ViewState) {
        if (state == ViewState.SYNC_COMPLETED) hideHomeButtonCommand.consumer.accept(Unit)
        else showHomeButtonCommand.consumer.accept(Unit)
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
