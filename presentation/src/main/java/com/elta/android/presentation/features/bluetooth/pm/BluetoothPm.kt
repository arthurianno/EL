@file:Suppress("VariableNaming", "MaxLineLength", "MagicNumber", "LongMethod")

package com.elta.android.presentation.features.bluetooth.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.FirmwareDownloadingError
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.ConnectDeviceUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometerEventsUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometerInfoUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.UpdateDeviceFirmwareUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.firmware.interactor.GetFirmwareInfoUseCase
import com.elta.android.domain.features.firmware.interactor.GetFirmwareUseCase
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.adapter.items.DeviceItem
import com.elta.android.presentation.messages.SnackBarMessageData
import io.reactivex.Observable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.inputControl
import timber.log.Timber
import javax.inject.Inject

class BluetoothPm @Inject constructor(
    private val updateDeviceFirmwareUseCase: UpdateDeviceFirmwareUseCase,
    private val getFirmwareInfoUseCase: GetFirmwareInfoUseCase,
    private val downloadFirmwareUseCase: GetFirmwareUseCase,
    private val setPinCodeUseCase: ConnectDeviceUseCase,
    private val getGlucometerEventsUseCase: GetGlucometerEventsUseCase,
    private val getGlucometerInfoUseCase: GetGlucometerInfoUseCase,
    private val getGlucometersUseCase: GetGlucometersUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val getInfoAction = action<Unit>()
    val getEventsAction = action<Unit>()
    val setPinAction = action<Unit>()
    val pinEnabledState = state(false)

    val checkFirmwareAction = action<Unit>()
    val downloadFirmwareAction = action<Unit>()
    val updateFirmwareAction = action<Unit>()

    val firmwareState = state<Firmware>()
    val firmwareFileState = state<FirmwareFile>()
    val downloadEnabledState = state(false)
    val updateEnabledState = state(false)

    var glucometer: Glucometer? = null

    private val scanResults = mutableSetOf<Glucometer>()
    val pinInputControl = inputControl()
    val logState = state("Log:")
    val requestEnableBluetoothCommand = command<Unit>(bufferSize = 1)
    val requestLocationPermissionsCommand = command<Unit>(bufferSize = 1)
    val requestEnableLocationCommand = command<Unit>(bufferSize = 1)

    val bluetoothEnabledAction = action<Unit>()
    val locationPermissionsGrantedAction = action<Unit>()
    val locationEnabledAction = action<Unit>()
    val startScanAction = action<Unit>()

    val openPinCodeDialogCommand = command<String>(bufferSize = 1)

    override fun onCreate() {
        super.onCreate()

        startScanAction.observable
            .flatMapSingle {
                getGlucometersUseCase.execute()
                    .doOnSuccess { results ->
                        scanResults.clear()
                        scanResults.addAll(results)
                        items.consumer.accept(
                            results.map { meter ->
                                DeviceItem(
                                    id = meter.id,
                                    name = meter.name ?: "Unknown device",
                                    address = meter.address,
                                    isSelected = meter.address == glucometer?.address,
                                    isLast = false
                                )
                            }
                        )
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        getInfoAction.observable
            .skipWhileInProgress()
            .map(::createInfoUseCaseParams)
            .flatMapSingle { params ->
                getGlucometerInfoUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .map { it.toString() }
                    .doOnSuccess(::writeToLog)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        getEventsAction.observable
            .skipWhileInProgress()
            .map(::createEventsUseCaseParams)
            .flatMapSingle { params ->
                getGlucometerEventsUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .map { it.joinToString("\n") }
                    .doOnSuccess(::writeToLog)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        setPinAction.observable
            .skipWhileInProgress()
            .filter { glucometer != null }
            .map(::createPinCodeUseCaseParams)
            .flatMapCompletable { params ->
                setPinCodeUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete { writeToLog("Pin set ok") }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        checkFirmwareAction.observable
            .skipWhileInProgress()
            .flatMapSingle { params ->
                getFirmwareInfoUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess {
                        firmwareState.consumer.accept(it)
                        downloadEnabledState.consumer.accept(true)
                    }
                    .map { it.toString() }
                    .doOnSuccess(::writeToLog)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        downloadFirmwareAction.observable
            .skipWhileInProgress()
            .map(::createGetFirmwareUseCaseParams)
            .flatMapSingle { params ->
                downloadFirmwareUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess {
                        firmwareFileState.consumer.accept(it)
                        enableUpdateButton()
                    }
                    .map { it.toString() }
                    .doOnSuccess(::writeToLog)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        updateFirmwareAction.observable
            .skipWhileInProgress()
            .map(::createUpdateFirmwareUseCaseParams)
            .flatMap { params ->
                updateDeviceFirmwareUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnNext {
                        writeToLog(it)
                    }
                    .doOnComplete {
                        writeToLog("Firmware updated")
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.DeviceClicked>()
            .doOnNext { click ->
                if (click.item.address != glucometer?.address) {
                    glucometer = scanResults.firstOrNull { it.address == click.item.address }
                    val newItems =
                        items.value.map { (it as DeviceItem).copy(isSelected = it.address == click.item.address) }
                    items.consumer.accept(newItems)
                }
                enableUpdateButton()
            }
            .subscribe()
            .untilDestroy()

        pinInputControl.textChanges.observable
            .map { it.length == 3 }
            .subscribe(pinEnabledState.consumer)
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
            is LocationPermissionNotGrantedError -> requestLocationPermissionsCommand.consumer.accept(
                Unit
            )
            is LocationNotEnabledError -> requestEnableLocationCommand.consumer.accept(Unit)
            is GlucometerPinIncorrectOrNotFoundError -> {
                openPinCodeDialogCommand.consumer.accept("SatelliteOnline")
                Timber.e(error)
//                showSnackBar(SnackBarMessageData.SimpleTextMessage("Enter pin code at input field and press SET PIN"))
            }
            is FirmwareDownloadingError -> showSnackBar(SnackBarMessageData.SimpleTextMessage("Firmware file is invalid"))
            else -> super.handleError(error)
        }
    }

    private fun writeToLog(response: String) {
        val log = logState.value
        logState.consumer.accept("$log\n====================\n$response")
    }

    private fun createInfoUseCaseParams(i: Unit): GetGlucometerInfoUseCase.Params =
        GetGlucometerInfoUseCase.Params(address = glucometer?.address ?: "")

    private fun createEventsUseCaseParams(i: Unit): GetGlucometerEventsUseCase.Params =
        GetGlucometerEventsUseCase.Params(address = glucometer?.address ?: "")

    private fun createPinCodeUseCaseParams(i: Unit): ConnectDeviceUseCase.Params =
        ConnectDeviceUseCase.Params(
            device = checkNotNull(glucometer),
            pinCode = pinInputControl.text.valueOrNull ?: ""
        )

    private fun createGetFirmwareUseCaseParams(i: Unit): GetFirmwareUseCase.Params =
        GetFirmwareUseCase.Params(firmwareState.value)

    private fun createUpdateFirmwareUseCaseParams(i: Unit): UpdateDeviceFirmwareUseCase.Params =
        UpdateDeviceFirmwareUseCase.Params(
            address = glucometer?.address ?: "",
            file = firmwareFileState.value
        )

    private fun enableUpdateButton() {
        val enable = glucometer != null
        updateEnabledState.consumer.accept(enable)
    }
}
