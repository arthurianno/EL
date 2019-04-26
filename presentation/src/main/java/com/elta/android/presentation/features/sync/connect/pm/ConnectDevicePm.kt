package com.elta.android.presentation.features.sync.connect.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SetPinCodeUseCase
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
import io.reactivex.Observable
import java.util.concurrent.TimeoutException
import javax.inject.Inject

class ConnectDevicePm @Inject constructor(
    private val setPinCodeUseCase: SetPinCodeUseCase,
    private val findGlucometersUseCase: FindGlucometersUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val mainAction = Action<Unit>()
    val skipAction = Action<Unit>()
    val connectDeviceAction = Action<Unit>()
    val connectDeviceEnabledState = State(false)

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

    private val scanResults = mutableSetOf<Glucometer>()
    private var glucometer: Glucometer? = null

    private val startScanAction = Action<Unit>()

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

    private val showRetrySearchAction = Action<Unit>()
    private val showRetryPinAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

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

        bus.events<Events.PinCodeEntered>()
            .skipWhileInProgress()
            .map(Events.PinCodeEntered::pin)
            .map { pin ->
                SetPinCodeUseCase.Params(glucometer?.address ?: "", pin)
            }
            .flatMapCompletable { params ->
                setPinCodeUseCase.execute(params)
                    .bindProgress()
                    .doOnComplete {
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
            else -> super.handleError(error)
        }
    }

    private fun navigateToShopsFlow(i: Unit) {
        router.newRootFlow(Screens.ShopsFlow)
    }

    enum class ViewState {
        SEARCH, FOUND, CONNECTED,
    }
}