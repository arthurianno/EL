package com.elta.android.data.features.devices.glucometer.service

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.elta.android.common.errors.BluetoothNotAvailableError
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.GlucometerPinRequireError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.command.Commands
import com.elta.android.data.features.devices.glucometer.refactor.GlucometerCommand
import com.elta.android.data.features.devices.glucometer.storage.GlucometerPinStorage
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.rxkotlin.Observables
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UtilService @Inject constructor(
    context: Context,
    private val client: RxBleClient,
    private val pinStorage: GlucometerPinStorage,
) {

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter = bluetoothManager.adapter
    val scanner: BluetoothLeScanner = adapter.bluetoothLeScanner

    val settings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(0)
        .build()

    val filters: List<ScanFilter> = listOf<ScanFilter>(
        ScanFilter.Builder().setDeviceName("Satellite").build()
    )

    val dfuFilters: List<ScanFilter> = listOf<ScanFilter>(
        ScanFilter.Builder().setDeviceName("Dfu").build()
    )

    val infoCommands: List<Commands> = listOf(
        Commands.GetDate,
        Commands.GetBatteryAndTemperature,
        Commands.GetVersion,
        Commands.Serial
    )

    fun checkBluetoothClientState(): Observable<RxBleClient.State> =
        Observable.just(client.state)
            .flatMap { observableState(it) }


    internal fun observableState(state: RxBleClient.State): ObservableSource<RxBleClient.State> =
        state.toError()?.let { Observable.error(it) }
            ?: Observable.just(state)

    private fun RxBleClient.State.toError(): Throwable? =
        when (this) {
            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> BluetoothNotAvailableError
            RxBleClient.State.BLUETOOTH_NOT_ENABLED -> BluetoothNotEnabledError
            RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED -> LocationPermissionNotGrantedError
            RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> LocationNotEnabledError
            else -> null
        }

    internal fun checkBattery(
        info: GlucometerInfoDto,
        connection: RxBleConnection,
        address: String
    ) = when {
        !info.isBatteryLevelEnoughForUpdate() -> Observable.error(
            GlucometerLowBatteryLevelError(
                current = info.batteryLevel ?: 0,
                required = MIN_BATTERY_LEVEL
            )
        )

        else -> request(connection, address, Commands.ToDfuMode)
    }

    internal fun request(
        connection: RxBleConnection,
        address: String,
        cmd: Commands
    ): Observable<String> {
        val input = cmd.command
        val notification = connection.setupNotification(UART_TX)
            .switchMap { it }
            .map { it.toString(Charset.defaultCharset()) }
        val command = connection.writeCharacteristic(
            UART_RX, input.toByteArray(
                Charset.defaultCharset()
            )
        )
            .toObservable().map { it.toString(Charset.defaultCharset()) }
        return Observables.combineLatest(notification.take(1), command) { response, _ -> response }
            .take(1)
            .compose {
                it.switchMap { response ->
                    when {
                        input.isPinCommand() && response.isPinError() -> {
                            pinStorage.setPin(address, "")
                            Observable.error(GlucometerPinIncorrectOrNotFoundError)
                        }

                        response.isPinError() -> Observable.error(GlucometerPinRequireError)
                        else -> Observable.just(response)
                    }
                }
            }
            .retryWhen { errors ->
                errors
                    .concatMap {
                        when (it) {
                            is GlucometerPinRequireError -> Observable.just(Unit)
                            else -> Observable.error(it)
                        }
                    }
                    .concatMap {
                        val pin = pinStorage.getPin(address)
                        when (pin.isNullOrEmpty()) {
                            true -> Observable.error(GlucometerPinIncorrectOrNotFoundError)
                            else -> request(connection, address, Commands.SetPin(pin))
                        }
                    }
            }
    }


}