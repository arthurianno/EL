package com.elta.android.data.features.devices.datasource

import com.elta.android.common.errors.BluetoothNotAvailableError
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.utils.log
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.domain.features.devices.model.Command
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable
import io.reactivex.Single
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
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

    private val connections = ConcurrentHashMap<RxBleDevice, RxBleConnection>()

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

    fun getGlucometerInfo(address: String, commands: List<Command>): Single<GlucometerInfo> {
        val device = client.getBleDevice(address)
        device.establishConnection(false).flatMap {
            it.setupNotification()
        }

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