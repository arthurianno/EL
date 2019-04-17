package com.elta.android.data.features.devices.glucometer

import android.content.Context
import com.elta.android.common.errors.BluetoothNotAvailableError
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.FirmwareNotSupportedByAppError
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.GlucometerPinRequireError
import com.elta.android.common.errors.GlucometerToDfuModeError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.utils.log
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.nio.charset.Charset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlucometersManager @Inject constructor(
    private val eventBuilder: GlucometerEventBuilder,
    private val pinStorage: GlucometerPinStorage,
    private val infoBuilder: GlucometerInfoBuilder,
    private val client: RxBleClient,
    private val context: Context
) {

    private val scanner = BluetoothLeScannerCompat.getScanner()

    private val settings = ScanSettings.Builder()
        .setLegacy(false)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(0)
        .setUseHardwareBatchingIfSupported(true)
        .build()

    private val filters = listOf<ScanFilter>(
        ScanFilter.Builder().setDeviceName("SatelliteOnline").build()
    )

    private val connections = mutableMapOf<String, RxBleConnection>()
    private val infoCommands = listOf(
        Commands.GetDate, Commands.GetBatteryAndTemperature, Commands.GetVersion
    )

    fun findDevices(): Observable<List<ScanResult>> =
        Observable.just(client.state)
            .flatMap { state ->
                val error = state.toError()
                if (error != null) Observable.error(error)
                else Observable.just(state)
            }
            .flatMap {
                scanner.startScan(filters, settings)
            }

    fun getGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        client.findConnection(address)
            .checkPinAndSend(address)
            .switchMap { connection ->
                connection.batchRequest(address, infoCommands)
            }
            .take(1)
            .map(infoBuilder::buildFrom)
            .singleOrError()

    fun getGlucometerEvents(address: String): Single<List<GlucometerEventDto>> =
        client.findConnection(address)
            .checkPinAndSend(address)
            .switchMap { connection ->
                Observable.range(0, EVENTS_COUNT)
                    .concatMap {
                        connection.request(address, Commands.ReadEvent(it))
                    }
            }
            .takeUntil { isPotentialLastEvent(it) }
            .collectInto(mutableListOf<String>()) { responses, response ->
                if (!isPotentialLastEvent(response)) responses.add(response)
            }
            // TODO: pass part of device name instead of address.
            // On iOS devices address can't be extracted
            // so events will have different id on Android and iOS platforms
            .map { it.map { response -> eventBuilder.buildFrom(address, response) } }

    fun setPinCode(address: String, pinCode: String): Completable =
        Completable.fromCallable {
            pinStorage.setPin(address, pinCode)
        }

    fun updateFirmware(address: String, file: FirmwareFile): Completable =
        when {
            !file.isSupportedByApplication() -> Completable.error(FirmwareNotSupportedByAppError(file.version))
            else -> client.findConnection(address)
                .checkPinAndSend(address)
                .switchMap { connection ->
                    connection.request(address, Commands.GetBatteryAndTemperature)
                        .map { infoBuilder.buildFrom(listOf(it)) }
                        .switchMap { info ->
                            when {
                                !info.isBatteryLevelEnoughForUpdate() -> Observable.error(
                                    GlucometerLowBatteryLevelError(
                                        current = info.batteryLevel ?: 0,
                                        required = MIN_LEVEL
                                    )
                                )
                                else -> connection.request(address, Commands.ToDfuMode)
                                    .log("BLE", "boot")
                            }
                        }
                }
                .take(1)
                .switchMapCompletable { response ->
                    when (isOk(response)) {
                        true -> startFirmwareUpdate(context, file.path, address)
                        else -> Completable.error(GlucometerToDfuModeError)
                    }
                }
        }

    private fun RxBleConnection.request(address: String, cmd: GlucometerCommand): Observable<String> {
        val input = cmd.toGlucometerString()
        val notification = setupNotification(UART_TX)
            .switchMap { it }
            .map { it.toString(Charset.defaultCharset()) }
        val command = writeCharacteristic(UART_RX, input.toByteArray(Charset.defaultCharset()))
            .toObservable().map { it.toString(Charset.defaultCharset()) }
        return Observables.combineLatest(notification.take(1), command) { response, _ -> response }
            .take(1)
            .compose {
                it.switchMap { response ->
                    when {
                        isPinCommand(input)
                            && isPinError(response) -> Observable.error(GlucometerPinIncorrectOrNotFoundError)
                        isPinError(response) -> Observable.error(GlucometerPinRequireError)
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
                            else -> request(address, Commands.SetPin(pin))
                        }
                    }
            }
    }

    private fun RxBleConnection.batchRequest(
        address: String,
        commands: List<GlucometerCommand>
    ): Observable<List<String>> =
        Observable.fromIterable(commands.map { cmd -> request(address, cmd) })
            .concatMap { it }
            .buffer(commands.size)

    private fun RxBleClient.findConnection(address: String): Observable<RxBleConnection> =
        Observable.just(getBleDevice(address))
            .switchMap { device ->
                val connection = connections[address]
                if (connection == null || device.connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED)
                    device.establishConnection(false)
                        .compose(ReplayingShare.instance())
                        .doOnNext { connections[address] = it }
                else Observable.just(connection)
            }

    private fun Observable<RxBleConnection>.checkPinAndSend(address: String): Observable<RxBleConnection> =
        this.switchMap { connection ->
            val pin = pinStorage.getPin(address)
            when (pin.isNullOrEmpty()) {
                true -> Observable.error(GlucometerPinIncorrectOrNotFoundError)
                else -> connection.request(address, Commands.SetPin(pin))
                    .switchMap { Observable.just(connection) }
            }
        }

    private fun isPinError(response: String): Boolean = response == "pin.error"
    private fun isPinCommand(command: String): Boolean = command.startsWith("pin")
    private fun isPotentialLastEvent(response: String): Boolean = response.contains("9595959595.895895")
    private fun isOk(response: String): Boolean = response.contains("ok")

    private fun FirmwareFile.isSupportedByApplication(): Boolean {
        val appVersionCode = FIRMWARE_VERSION.replace(".", "").toInt()
        val compatibleVersionCode = compatible.replace(".", "").toInt()
        return appVersionCode >= compatibleVersionCode
    }

    private fun GlucometerInfoDto.isBatteryLevelEnoughForUpdate(): Boolean = batteryLevel ?: 0 >= MIN_LEVEL

    private fun RxBleClient.State.toError(): Throwable? =
        when (this) {
            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> BluetoothNotAvailableError
            RxBleClient.State.BLUETOOTH_NOT_ENABLED -> BluetoothNotEnabledError
            RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED -> LocationPermissionNotGrantedError
            RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> LocationNotEnabledError
            else -> null
        }

    companion object {
        private const val FIRMWARE_VERSION = "1.6" // version of firmware supported by application
        private const val MIN_LEVEL = 1 // minimal level of battery required to start firmware update
        private val UART_RX = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
        private val UART_TX = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
        private const val EVENTS_COUNT = 1000
    }
}