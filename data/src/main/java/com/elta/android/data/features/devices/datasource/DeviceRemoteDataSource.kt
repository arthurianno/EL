package com.elta.android.data.features.devices.datasource

import com.elta.android.common.errors.BluetoothNotAvailableError
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.utils.log
import com.elta.android.common.utils.logAll
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.domain.features.devices.model.Command
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.nullgr.core.date.toStringWithFormat
import com.polidea.rxandroidble2.RxBleClient
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.nio.charset.Charset
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class DeviceRemoteDataSource @Inject constructor(
    private val client: RxBleClient
) : DeviceDataSource {

    private val UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

    private val scanner = BluetoothLeScannerCompat.getScanner()

    private val settings = ScanSettings.Builder()
        .setLegacy(false)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setReportDelay(500)
        .setUseHardwareBatchingIfSupported(true)
        .build()

    private val filters = listOf<ScanFilter>(
        ScanFilter.Builder().setDeviceName("SatelliteOnline").build(),
        ScanFilter.Builder().setDeviceName("EltaDFU").build()
    )

    override fun findDevices(): Observable<List<GlucometerDto>> =
        Observable.just(client.state)
            .log("RxBleClient", "state") { it.name }
            .flatMap { state ->
                val error = state.toError()
                if (error != null) Observable.error(error)
                else Observable.just(state)
            }
            .flatMap {
                scanner.startScan(filters, settings)
                    .map {
                        it.map { result ->
                            GlucometerDto(
                                id = result.device.address,
                                address = result.device.address,
                                name = if (!result.device.name.isNullOrEmpty()) result.device.name else result.scanRecord?.deviceName,
                                device = result.device
                            )
                        }
                    }
            }

    override fun getGlucometerInfo(address: String, commands: List<Command>): Single<GlucometerInfo> {
        val device = client.getBleDevice(address)
        return device.establishConnection(false)
            .switchMapSingle { conn ->
                val obs = mutableListOf<Single<Pair<String, String>>>()
                commands.forEach { command ->
                    obs.add(
                        Singles.zip(
                            conn.writeCharacteristic(UART_RX_CHARACTERISTIC_UUID, command.toByteArray()).map { it.toString(Charset.defaultCharset()) },
                            conn.setupNotification(UART_TX_CHARACTERISTIC_UUID).flatMap { it }.map { it.toString(Charset.defaultCharset()) }.singleOrError()
                        )
                    )
                }

                Single.zip(obs) { pairs -> pairs }

            }
            .map { it.map { it as Pair<String, String> } }
            .logAll("Glucometer", "pairs") { "command: ${it.first}, result: ${it.second}" }
            .map {
                GlucometerInfo()
            }
            .singleOrError()

    }

    private fun Command.toByteArray(): ByteArray {
        val str = when (this) {
            is Command.Reset -> "reset"
            is Command.ToDfuMode -> "boot"
            is Command.SetTime -> "settime.${(this.params as Date).toStringWithFormat("yyMMddHHmmss")}"
            is Command.GetDate -> "time"
            is Command.AddEvent -> "blood.296044"
            is Command.ReadEvent -> "rd.${this.params as Int}"
            is Command.GetVersion -> "ver"
            is Command.TurnOnAntiLossMode -> "lon"
            is Command.TurnOffAntiLossMode -> "loff"
            is Command.TurnOnFindMode -> "find"
            is Command.GetBatteryAndTemperature -> "bat"
            is Command.SetPin -> "pin.${this.params as Int}"
        }
        return str.toByteArray(Charset.defaultCharset())
    }

    private fun RxBleClient.State.toError(): Throwable? =
        when (this) {
            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> BluetoothNotAvailableError
            RxBleClient.State.BLUETOOTH_NOT_ENABLED -> BluetoothNotEnabledError
            RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED -> LocationPermissionNotGrantedError
            RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> LocationNotEnabledError
            else -> null
        }
}