package com.elta.android.presentation.features.bluetooth.pm

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import me.dmdev.rxpm.widget.inputControl
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import timber.log.Timber
import java.nio.charset.Charset
import java.util.UUID
import javax.inject.Inject


class BluetoothPm @Inject constructor(
    private val context: Context,
    private val client: RxBleClient,
    services: ServiceFacade
) : BasePm(services) {

    val writeAction = Action<Unit>()
    val command = Action<Unit>()
    var device: BluetoothDevice? = null

    /** Nordic UART Service UUID  */
    private val UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    /** RX characteristic UUID  */
    private val UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    /** TX characteristic UUID  */
    private val UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")


    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val settings = ScanSettings.Builder()
        .setLegacy(false)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setNumOfMatches(1)
        .setReportDelay(1000)
        .setUseHardwareBatchingIfSupported(true)
        .build()
    private val filters = listOf<ScanFilter>(ScanFilter.Builder().setDeviceName("SatelliteOnline").build())
    private val callback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Timber.tag("SCAN").d("onScanFailed $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Timber.tag("SCAN").d("onScanResult $callbackType, $result")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            Timber.tag("SCAN").d("onBatchScanResults")
            results.forEach {
                Timber.tag("SCAN").d("result: ${it.device.name}, ${it.device.address}")
            }
            if (results.isNotEmpty() && device == null) {
                Timber.d("onBatchScanResults set device")
                device = results[0].device
                scanner.stopScan(this)

                val bleDevice = client.getBleDevice(device?.address ?: "")
                    .establishConnection(false)
                    .takeUntil(lifecycleObservable.filter { it == Lifecycle.DESTROYED })
                    .compose(ReplayingShare.instance())
                    .doOnNext {
                        connectionObservable.consumer.accept(it)
                    }
                    .subscribe()
            }
        }
    }

    private val connectionObservable = State<RxBleConnection>()
    val commandInputControl = inputControl()
    val logState = State("Log:")

    override fun onCreate() {
        super.onCreate()
        scanner.startScan(filters, settings, callback)

        writeAction.observable
            .flatMap {
                connectionObservable.observable
                    .flatMapSingle {
                        it.writeCharacteristic(UART_RX_CHARACTERISTIC_UUID, commandInputControl.text.value.toByteArray(Charset.defaultCharset()))
                            .doOnSuccess { bytes ->
                                val command = bytes.toString(Charset.defaultCharset())
                                val log = logState.value
                                logState.consumer.accept("$log\nWrite: $command")
                                Timber.d("writeCharacteristic: $command")
                            }
                            .doOnError { error ->
                                val log = logState.value
                                logState.consumer.accept("$log\nWrite Error: ${error.message}")
                                Timber.e(error, "writeCharacteristic")
                            }
                    }
            }
            .retry()
            .subscribe()
            .untilDestroy()

        command.observable
            .flatMap {
                connectionObservable.observable
                    .flatMap {
                        it.setupNotification(UART_TX_CHARACTERISTIC_UUID)
                            .flatMap { it }
                            .doOnNext { bytes ->
                                val message = bytes.toString(Charset.defaultCharset())
                                val log = logState.value
                                logState.consumer.accept("$log\nRead: $message")
                                Timber.d("readCharacteristic: $message")
                            }
                            .doOnError { error ->
                                val log = logState.value
                                logState.consumer.accept("$log\nRead Error: ${error.message}")
                                Timber.e(error, "readCharacteristic")
                            }
                    }
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner.stopScan(callback)
    }
}