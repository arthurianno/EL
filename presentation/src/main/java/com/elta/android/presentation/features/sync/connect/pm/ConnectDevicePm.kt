package com.elta.android.presentation.features.sync.connect.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
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
import com.elta.android.presentation.features.sync.connect.ui.adapter.items.DeviceItem
import com.elta.android.presentation.messages.SnackBarMessageData
import com.nullgr.core.rx.bindProgress
import io.reactivex.Observable
import me.dmdev.rxpm.skipWhileInProgress
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class ConnectDevicePm @Inject constructor(
    private val syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    private val connectDeviceUseCase: ConnectDeviceUseCase,
    private val findGlucometersUseCase: FindGlucometersUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val skipAction = Action<Unit>()

    val connectDeviceAction = Action<Unit>()
    val connectDeviceEnabledState = State(false)

    val toAppAction = Action<Unit>()

    val openPinCodeDialogCommand = Command<String>(bufferSize = 1)

    val state = State(ViewState.SEARCH)

    val requestEnableBluetoothCommand = Command<Unit>(bufferSize = 1)
    val requestLocationPermissionsCommand = Command<Unit>(bufferSize = 1)
    val requestEnableLocationCommand = Command<Unit>(bufferSize = 1)

    val bluetoothEnabledAction = Action<Unit>()
    val locationPermissionsGrantedAction = Action<Unit>()
    val locationEnabledAction = Action<Unit>()

    val retrySearchControl = snackBarControl<SnackBarData>()
    val retryPinControl = snackBarControl<SnackBarData>()
    val retrySyncControl = snackBarControl<SnackBarData>()

    private val scanResults = mutableSetOf<Glucometer>()
    private var glucometer: Glucometer? = null

    private val startScanAction = Action<Unit>()
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

    private val syncError: SnackBarData by lazy {
        SnackBarMessageData.WithButton(
            resources.getString(R.string.sync_connect_sync_error),
            resources.getString(R.string.sync_connect_button_retry)
        )
    }

    private val showRetrySearchAction = Action<Unit>()
    private val showRetryPinAction = Action<Unit>()
    private val showRetrySyncAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()
        bindActions()
        bindClicksAndEvents()

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
            bluetoothEnabledAction.observable,
            locationPermissionsGrantedAction.observable,
            locationEnabledAction.observable
        )
            .subscribe(startScanAction.consumer)
            .untilDestroy()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is BluetoothNotEnabledError -> requestEnableBluetoothCommand.consumer.accept(Unit)
            is LocationPermissionNotGrantedError -> requestLocationPermissionsCommand.consumer.accept(Unit)
            is LocationNotEnabledError -> requestEnableLocationCommand.consumer.accept(Unit)
            is TimeoutException -> {
                val devices = items.valueOrNull
                if (devices == null || devices.isEmpty()) {
                    showRetrySearchAction.consumer.accept(Unit)
                }
            }
            is GlucometerPinIncorrectOrNotFoundError -> showRetryPinAction.consumer.accept(Unit)
            is GlucometerSyncError -> showRetrySyncAction.consumer.accept(Unit)
            else -> super.handleError(error)
        }
    }

    private fun bindActions() {
        startScanAction.observable
            .flatMap {
                findGlucometersUseCase.execute()
                    .doOnNext { results ->
                        if (results.isNotEmpty()) {
                            state.consumer.accept(ViewState.FOUND)
                            retrySearchControl.dismiss()
                        }
                        scanResults.clear()
                        scanResults.addAll(results)
                        items.consumer.accept(
                            results.mapIndexed { index, meter ->
                                DeviceItem(
                                    id = meter.id,
                                    name = meter.name ?: "Unknown device",
                                    address = meter.address,
                                    isSelected = meter.address == glucometer?.address,
                                    isTheLast = index == results.size - 1
                                )
                            }
                        )
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

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

        showRetrySyncAction.observable
            .switchMapMaybe {
                retrySyncControl.showForResult(syncError)
            }
            .subscribe(startSyncAction.consumer)
            .untilDestroy()

        skipAction.observable
            .doOnNext(::navigateToShopsFlow)
            .subscribe()
            .untilDestroy()

        connectDeviceAction.observable
            .skipWhileInProgress()
            .debounceAction()
            .map { glucometer?.name ?: "SatelliteOnline" }
            .subscribe(openPinCodeDialogCommand.consumer)
            .untilDestroy()

        toAppAction.observable
            .subscribe { router.newRootFlow(Screens.HomeFlow) }
            .untilDestroy()

        startSyncAction.observable
            .skipWhileInProgress(syncProgressState.observable)
            .filter { glucometer != null }
            .map {
                SyncWithGlucometerUseCase.Params(glucometer)
            }
            .flatMapCompletable { params ->
                syncWithGlucometerUseCase.execute(params)
                    .bindProgress(syncProgressState.consumer)
                    .doOnComplete {
                        state.consumer.accept(ViewState.SYNC_COMPLETED)
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindClicksAndEvents() {
        bus.events<Events.PinCodeEntered>()
            .skipWhileInProgress()
            .filter { glucometer != null }
            .map(Events.PinCodeEntered::pin)
            .map { pin ->
                ConnectDeviceUseCase.Params(checkNotNull(glucometer), pin)
            }
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

    enum class ViewState {
        SEARCH, FOUND, CONNECTED, SYNC_COMPLETED
    }
}