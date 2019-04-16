@file:Suppress("VariableNaming", "MaxLineLength", "MagicNumber", "LongMethod")

package com.elta.android.presentation.features.bluetooth.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.FirmwareDownloadingError
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometerEventsUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometerInfoUseCase
import com.elta.android.domain.features.devices.interactor.SetPinCodeUseCase
import com.elta.android.domain.features.devices.interactor.UpdateDeviceFirmwareUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.firmware.interactor.DownloadFirmwareUseCase
import com.elta.android.domain.features.firmware.interactor.GetFirmwareInfoUseCase
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.bluetooth.ui.adapter.items.DeviceItem
import com.elta.android.presentation.messages.SnackBarMessageData
import io.reactivex.Observable
import me.dmdev.rxpm.widget.inputControl
import javax.inject.Inject

class BluetoothPm @Inject constructor(
    private val updateDeviceFirmwareUseCase: UpdateDeviceFirmwareUseCase,
    private val getFirmwareInfoUseCase: GetFirmwareInfoUseCase,
    private val downloadFirmwareUseCase: DownloadFirmwareUseCase,
    private val setPinCodeUseCase: SetPinCodeUseCase,
    private val getGlucometerEventsUseCase: GetGlucometerEventsUseCase,
    private val getGlucometerInfoUseCase: GetGlucometerInfoUseCase,
    private val findGlucometersUseCase: FindGlucometersUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val getInfoAction = Action<Unit>()
    val getEventsAction = Action<Unit>()
    val setPinAction = Action<Unit>()
    val pinEnabledState = State(false)

    val checkFirmwareAction = Action<Unit>()
    val downloadFirmwareAction = Action<Unit>()
    val updateFirmwareAction = Action<Unit>()

    val firmwareState = State<Firmware>()
    val firmwareFileState = State<FirmwareFile>()
    val downloadEnabledState = State(false)
    val updateEnabledState = State(false)

    var glucometer: Glucometer? = null

    private val scanResults = mutableSetOf<Glucometer>()
    val pinInputControl = inputControl()
    val logState = State("Log:")
    val requestEnableBluetoothCommand = Command<Unit>(bufferSize = 1)
    val requestLocationPermissionsCommand = Command<Unit>(bufferSize = 1)
    val requestEnableLocationCommand = Command<Unit>(bufferSize = 1)

    val bluetoothEnabledAction = Action<Unit>()
    val locationPermissionsGrantedAction = Action<Unit>()
    val locationEnabledAction = Action<Unit>()
    val startScanAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        startScanAction.observable
            .flatMap {
                findGlucometersUseCase.execute()
                    .doOnNext { results ->
                        scanResults.clear()
                        scanResults.addAll(results)
                        items.consumer.accept(
                            results.map { meter ->
                                DeviceItem(
                                    id = meter.id,
                                    name = meter.name ?: "Unknown device",
                                    address = meter.address,
                                    isSelected = meter.address == glucometer?.address
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
            .map(::createPinCodeUseCaseParams)
            .flatMapCompletable { params ->
                setPinCodeUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete { writeToLog("Pin set") }
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
            .flatMapCompletable { params ->
                updateDeviceFirmwareUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnComplete {
                        writeToLog("Firmware update started")
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
                    val newItems = items.value.map { (it as DeviceItem).copy(isSelected = it.address == click.item.address) }
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
            is LocationPermissionNotGrantedError -> requestLocationPermissionsCommand.consumer.accept(Unit)
            is LocationNotEnabledError -> requestEnableLocationCommand.consumer.accept(Unit)
            is GlucometerPinIncorrectOrNotFoundError -> showSnackBar(SnackBarMessageData.SimpleTextMessage("Enter pin code at input field and press SET PIN"))
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

    private fun createPinCodeUseCaseParams(i: Unit): SetPinCodeUseCase.Params =
        SetPinCodeUseCase.Params(
            address = glucometer?.address ?: "",
            pinCode = pinInputControl.text.valueOrNull ?: ""
        )

    private fun createGetFirmwareUseCaseParams(i: Unit): DownloadFirmwareUseCase.Params =
        DownloadFirmwareUseCase.Params(firmwareState.value)

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