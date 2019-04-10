package com.elta.android.data.features.devices.datasource

import com.elta.android.common.errors.BluetoothNotAvailableError
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.utils.log
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.domain.features.devices.model.Command
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.jakewharton.rx.ReplayingShare
import com.nullgr.core.date.toDate
import com.nullgr.core.date.toStringWithFormat
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Observables
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.nio.charset.Charset
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@Suppress("MagicNumber")
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

    private val connections = mutableMapOf<String, RxBleConnection>()
    private val notifications = mutableMapOf<String, Observable<String>>()

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
                                name = if (!result.device.name.isNullOrEmpty()) result.device.name
                                else result.scanRecord?.deviceName,
                                device = result.device
                            )
                        }
                    }
            }

//    override fun getGlucometerInfo(address: String, commands: List<Command>): Single<GlucometerInfo> {
//        return Observable.just(client.getBleDevice(address))
//            .switchMap { device ->
//                val connection = connections[address]
//                if (connection == null) device.establishConnection(false)
//                    .compose(ReplayingShare.instance())
//                    .doOnNext { connections[address] = it }
//                else Observable.just(connection)
//            }
//            .switchMap { connection ->
//                val notification = notifications[address]
//                if (notification == null) connection.setupNotification(UART_TX_CHARACTERISTIC_UUID)
//                    .switchMap { it }
//                    .switchMap { Observable.just(Pair(connection, Observable.just(it.toString(Charset.defaultCharset())))) }
//                else Observable.just(Pair(connection, notification))
//            }
////            .switchMap {
////                Observables.combineLatest(
////                    it.first.write("pin.121").log("BLE", "write").take(1),
////                    it.second.log("BLE", "read").take(1)
////                )
////            }
//            .switchMapSingle { it.first.writeSingle("pin.121").doOnSuccess { Timber.d("BLE write $it") } }.doOnError { Timber.e(it, "BLE write") }
//            .switchMapSingle {
//                Single.just(GlucometerInfo())
//            }.singleOrError()
//    }

    override fun getGlucometerInfo(address: String, commands: List<Command>): Single<GlucometerInfo> {
        return Observable.just(client.getBleDevice(address))
            .switchMap { device ->
                val connection = connections[address]
                if (connection == null || device.connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED) device.establishConnection(false)
                    .compose(ReplayingShare.instance())
                    .doOnNext { connections[address] = it }
                else Observable.just(connection)
            }
//            .switchMap { connection ->
//                Observables.combineLatest(
//                    connection.setupNotification(UART_TX_CHARACTERISTIC_UUID).switchMap { it }.map { it.toString(Charset.defaultCharset()) }.doOnNext { Timber.d("BLE read $it") }.doOnError { Timber.e(it, "BLE read") },
//                    connection.write("pin.286").doOnNext { Timber.d("BLE write $it") }.doOnError { Timber.e(it, "BLE write") }
//                )
//            }
            // works
//            .switchMap { connection ->
//                connection.request("pin.286").log("BLE", "request") { "send: ${it.request}, received: ${it.response}" }
//            }
            // works
//            .switchMap { connection ->
//                connection.batchRequest("pin.286", "time", "ver").logAll("BLE", "batch") { "send: ${it.request}, received: ${it.response}" }
//            }
            .switchMap { connection ->
                connection.batchCommandRequest(Command.SetPin(286), Command.GetDate, Command.GetBatteryAndTemperature, Command.GetVersion)
                    .log("BLE", "batch") { it.entries.joinToString { "send: command: ${it.key::class.java.simpleName}, received: ${it.value.response}, payload: ${it.key.getPayload(it.value)}" } }
            }
            .take(1)
            .map {
                GlucometerInfo(
                    date = Command.GetDate.parsePayload(Command.GetDate.getPayload(it[Command.GetDate])) as Date
                )
            }.singleOrError()
    }

    private fun RxBleConnection.request(input: ByteArray): Observable<ResponseWrapper<ByteArray>> {
        val notification = setupNotification(UART_TX_CHARACTERISTIC_UUID).switchMap { it }
        val command = writeCharacteristic(UART_RX_CHARACTERISTIC_UUID, input).toObservable()
        return Observables.combineLatest(notification.take(1), command) { response, request -> ResponseWrapper(request, response) }.take(1)
    }

    private fun RxBleConnection.request(input: String): Observable<ResponseWrapper<String>> {
        val notification = setupNotification(UART_TX_CHARACTERISTIC_UUID).switchMap { it }.map { it.toString(Charset.defaultCharset()) }
        val command = writeCharacteristic(UART_RX_CHARACTERISTIC_UUID, input.toByteArray(Charset.defaultCharset())).toObservable().map { it.toString(Charset.defaultCharset()) }
        return Observables.combineLatest(notification.take(1), command) { response, request -> ResponseWrapper(request, response) }.take(1)
    }

    private fun RxBleConnection.batchRequest(vararg inputs: String): Observable<List<ResponseWrapper<String>>> =
        Observable.fromIterable(inputs.map { input -> request(input) })
            .concatMap { it }
            .buffer(inputs.size)

    private fun RxBleConnection.batchCommandRequest(vararg commands: Command): Observable<Map<Command, ResponseWrapper<String>>> =
        Observable.fromIterable(commands.asIterable())
            .concatMap { command ->
                Observables.zip(Observable.just(command), request(command.toRequestString()))
            }
            .buffer(commands.size)
            .map { it.toMap() }


    private fun Command.toRequestString(): String =
        when (this) {
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

    private fun Command.toByteArray(): ByteArray = toRequestString().toByteArray(Charset.defaultCharset())

    private fun Command.getPayload(response: ResponseWrapper<String>?): String {
        if (response == null) return ""
        return when (this) {
            is Command.Reset -> response.response.split(".")[1]
            is Command.ToDfuMode -> response.response.split(".")[1]
            is Command.SetTime -> response.response.split(".")[1]
            is Command.GetDate -> response.response.split(".")[1]
            is Command.AddEvent -> response.response.split(".")[1]
            is Command.ReadEvent -> response.response.split(".")[1]
            is Command.GetVersion -> response.response
            is Command.TurnOnAntiLossMode -> response.response.split(".")[1]
            is Command.TurnOffAntiLossMode -> response.response.split(".")[1]
            is Command.TurnOnFindMode -> response.response.split(".")[1]
            is Command.GetBatteryAndTemperature -> response.response
            is Command.SetPin -> response.response.split(".")[1]
        }
    }

    private fun Command.parsePayload(payload: String): Any? =
        when (this) {
            is Command.Reset -> payload
            is Command.ToDfuMode -> payload
            is Command.SetTime -> payload
            is Command.GetDate -> payload.toDate("yyMMddHHmmss")
            is Command.AddEvent -> payload
            is Command.ReadEvent -> {
                val tokens = payload.split(".")
                val date = tokens[0].toDate("yyMMddHHmm")
                val temperature = tokens[1].substring(0, 3)
                val level = tokens[1].substring(3, 6).toDouble()
                level
            }
            is Command.GetVersion -> {
                val tokens = payload.split(" ")
                val soft = tokens[0].removePrefix("Soft").toDouble()
                val hard = tokens[0].removePrefix("hard").toDouble()
                Pair(soft, hard)
            }
            is Command.TurnOnAntiLossMode -> payload
            is Command.TurnOffAntiLossMode -> payload
            is Command.TurnOnFindMode -> payload
            is Command.GetBatteryAndTemperature -> {
                val tokens = payload.split("")
                val battery = tokens[0].removePrefix("b").toInt()
                val temperature = tokens[1].removePrefix("t").toInt()
                Pair(battery, temperature)
            }
            is Command.SetPin -> payload
        }

    private fun RxBleClient.State.toError(): Throwable? =
        when (this) {
            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> BluetoothNotAvailableError
            RxBleClient.State.BLUETOOTH_NOT_ENABLED -> BluetoothNotEnabledError
            RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED -> LocationPermissionNotGrantedError
            RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> LocationNotEnabledError
            else -> null
        }

    private class ResponseWrapper<T>(val request: T, val response: T)
}