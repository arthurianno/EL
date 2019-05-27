package com.elta.android.presentation.features.sync.connect.base.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.ConnectDeviceUseCase
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.snackBarControl
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.elta.android.presentation.features.sync.control.bluetoothControl
import com.elta.android.presentation.messages.SnackBarMessageData
import com.nullgr.core.rx.bindProgress
import io.reactivex.Observable
import me.dmdev.rxpm.skipWhileInProgress
import java.util.concurrent.TimeoutException

abstract class ConnectDevicePm constructor(
    private val syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    private val connectDeviceUseCase: ConnectDeviceUseCase,
    private val findGlucometersUseCase: FindGlucometersUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val skipAction = Action<Unit>()

    val connectDeviceAction = Action<Unit>()
    val connectDeviceEnabledState = State(false)

    val startScanAction = Action<Unit>()
    val toAppAction = Action<Unit>()

    val openPinCodeDialogCommand = Command<String>(bufferSize = 1)

    val state = State(ViewState.HOW_TO_CONNECT)

    val btControl = bluetoothControl()

    val retrySearchControl = snackBarControl<SnackBarData>()
    val retryPinControl = snackBarControl<SnackBarData>()
    val retryConnectControl = snackBarControl<SnackBarData>()
    val retrySyncControl = snackBarControl<SnackBarData>()

    private val scanResults = mutableSetOf<Glucometer>()
    private var glucometer: Glucometer? = null

    private val startSyncAction = Action<Unit>()
    private val syncProgressState = State(false)

    private val deviceNotFound: SnackBarData by lazy {
        SnackBarMessageData.WithButton(
            resources.getString(R.string.sync_connect_device_not_found),
            resources.getString(R.string.sync_connect_button_retry)
        )
    }

    private val incorrectPinCode: SnackBarData by lazy {
        SnackBarMessageData.WithButton(
            resources.getString(R.string.sync_connect_incorrect_pin_code),
            resources.getString(R.string.sync_connect_button_retry)
        )
    }

    private val connectError: SnackBarData by lazy {
        SnackBarMessageData.WithButton(
            resources.getString(R.string.sync_connect_connect_error),
            resources.getString(R.string.sync_connect_button_retry)
        )
    }

    private val syncError: SnackBarData by lazy {
        SnackBarMessageData.WithButton(
            resources.getString(R.string.sync_connect_sync_error),
            resources.getString(R.string.sync_connect_button_retry)
        )
    }

    private val showRetrySearchAction = Action<Unit>()
    private val showRetryPinAction = Action<Unit>()
    private val showRetrySyncAction = Action<Unit>()
    private val showRetryConnectAction = Action<Unit>()

    private val internalConnectDeviceAction = Action<Unit>()
    private val pinState = State<String>()

    protected abstract fun navigateToApp(i: Unit)

    override fun onCreate() {
        super.onCreate()
        bindActions()
        bindRetryActions()
        bindClicksAndEvents()

        Observable.merge(
            btControl.bluetoothEnabledAction.observable,
            btControl.locationPermissionsGrantedAction.observable,
            btControl.locationEnabledAction.observable
        )
            .subscribe(startScanAction.consumer)
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is BluetoothNotEnabledError -> btControl.requestEnableBluetoothCommand.consumer.accept(Unit)
            is LocationPermissionNotGrantedError -> btControl.requestLocationPermissionsCommand.consumer.accept(Unit)
            is LocationNotEnabledError -> btControl.requestEnableLocationCommand.consumer.accept(Unit)
            is TimeoutException -> {
                val devices = items.valueOrNull
                if (devices == null || devices.isEmpty()) {
                    showRetrySearchAction.consumer.accept(Unit)
                }
            }
            is GlucometerPinIncorrectOrNotFoundError -> showRetryPinAction.consumer.accept(Unit)
            is GlucometerSyncError -> showRetrySyncAction.consumer.accept(Unit)
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
            .map { glucometer?.name ?: "SatelliteOnline" }
            .subscribe(openPinCodeDialogCommand.consumer)
            .untilDestroy()

        internalConnectDeviceAction.observable
            .skipWhileInProgress()
            .filter { glucometer != null && pinState.hasValue() }
            .map { ConnectDeviceUseCase.Params(checkNotNull(glucometer), pinState.value) }
            .flatMapCompletable { params ->
                connectDeviceUseCase.execute(params)
                    .bindProgress()
                    .doOnComplete {
                        startSyncAction.consumer.accept(Unit)
                        state.consumer.accept(ViewState.CONNECTED)
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        toAppAction.observable
            .subscribe(::navigateToApp)
            .untilDestroy()
    }

    private fun bindStartScanAction() {
        startScanAction.observable
            .flatMap {
                findGlucometersUseCase.execute()
                    .doOnSubscribe {
                        state.consumer.accept(ViewState.SEARCH)
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
            .skipWhileInProgress(syncProgressState.observable)
            .filter { glucometer != null }
            .map { SyncWithGlucometerUseCase.Params(glucometer) }
            .flatMapCompletable { params ->
                syncWithGlucometerUseCase.execute(params)
                    .bindProgress(syncProgressState.consumer)
                    .doOnComplete { state.consumer.accept(ViewState.SYNC_COMPLETED) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindRetryActions() {
        showRetrySearchAction.observable
            .switchMapMaybe {
                retrySearchControl.showForResult(deviceNotFound)
            }
            .subscribe(startScanAction.consumer)
            .untilDestroy()

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

        showRetrySyncAction.observable
            .switchMapMaybe {
                retrySyncControl.showForResult(syncError)
            }
            .subscribe(startSyncAction.consumer)
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
                glucometer = if (glucometer?.address != click.item.address) {
                    scanResults.firstOrNull { it.address == click.item.address }
                } else null
                val prevItems = items.value
                val newItems = prevItems.mapIndexed { index, item ->
                    (item as DeviceItem).copy(
                        isSelected = item.address == click.item.address && !item.isSelected,
                        isTheLast = index == prevItems.size - 1
                    )
                }
                items.consumer.accept(newItems)
                connectDeviceEnabledState.consumer.accept(glucometer != null)
            }
            .subscribe()
            .untilDestroy()
    }

    private fun navigateToShopsFlow(i: Unit) {
        router.newRootFlow(Screens.ShopsFlow)
    }

    private fun handleSearchResults(results: List<Glucometer>) {
        if (results.isNotEmpty()) {
            state.consumer.accept(ViewState.FOUND)
            retrySearchControl.dismiss()
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
            isTheLast = index == size - 1
        )

    enum class ViewState {
        HOW_TO_CONNECT, SEARCH, FOUND, CONNECTED, SYNC_COMPLETED
    }
}