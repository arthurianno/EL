package com.elta.android.presentation.features.bluetooth.pm

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import com.elta.android.common.utils.log
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.bluetooth.EltaDfuService
import com.elta.android.presentation.features.bluetooth.startScan
import com.elta.android.presentation.features.bluetooth.ui.adapter.items.DeviceItem
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import me.dmdev.rxpm.widget.inputControl
import no.nordicsemi.android.dfu.DfuProgressListener
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
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
) : BaseListPm(services) {

    private val UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val settings = ScanSettings.Builder()
        .setLegacy(false)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setNumOfMatches(1)
        .setReportDelay(1000)
        .setUseHardwareBatchingIfSupported(true)
        .build()
    private val filters = listOf<ScanFilter>(
        ScanFilter.Builder().setDeviceName("SatelliteOnline").build(),
        ScanFilter.Builder().setDeviceName("EltaDFU").build()
    )

    private val dfuListener: DfuProgressListener = object : DfuProgressListenerAdapter() {
        override fun onDeviceConnected(deviceAddress: String) {
            Timber.d("onDeviceConnected $deviceAddress")
        }

        override fun onDeviceDisconnected(deviceAddress: String) {
            Timber.d("onDeviceDisconnected $deviceAddress")
        }

        override fun onDfuProcessStarted(deviceAddress: String) {
            Timber.d("onDfuProcessStarted $deviceAddress")
        }

        override fun onProgressChanged(deviceAddress: String?, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            Timber.d("onProgressChanged $percent")
        }

        override fun onDfuCompleted(deviceAddress: String?) {
            Timber.d("onDfuCompleted")
        }
    }

    val writeAction = Action<Unit>()
    val connectAction = Action<Unit>()
    val dfuAction = Action<Unit>()
    var device: BluetoothDevice? = null

    private val scanResults = mutableSetOf<ScanResult>()
    private val connectionObservable = State<RxBleConnection>()
    val commandInputControl = inputControl()
    val logState = State("Log:")
    val requestPermissionsCommand = Command<Unit>()
    val startScanAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        startScanAction.observable
            .flatMap {
                scanner.startScan(filters, settings)
                    .doOnNext { results ->
                        scanResults.clear()
                        scanResults.addAll(results)
                        items.consumer.accept(
                            results.map {
                                DeviceItem(
                                    id = it.device.address,
                                    name = if (!it.device.name.isNullOrEmpty()) it.device.name else it.scanRecord?.deviceName
                                        ?: "Unknown name",
                                    address = it.device.address,
                                    isSelected = it.device.address == device?.address
                                )
                            }
                        )
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        client.observeStateChanges()
            .log("Bluetooth", "state") { it.name }
            .doOnNext { state ->
                when (state) {
                    RxBleClient.State.READY -> startScanAction.consumer.accept(Unit)
                    else -> {
                    }
                }
            }
            .retry()
            .subscribe()
            .untilDestroy()

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

        connectAction.observable
            .flatMap {
                client.getBleDevice(device?.address ?: "")
                    .establishConnection(false)
                    .takeUntil(lifecycleObservable.filter { it == Lifecycle.DESTROYED })
                    .compose(ReplayingShare.instance())
                    .doOnNext { connectionObservable.consumer.accept(it) }
                    .doOnError {
                        Timber.e(it, "establishConnection")
                    }
            }
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

        dfuAction.observable
            .doOnNext {
                device?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        DfuServiceInitiator.createDfuNotificationChannel(context)
                    }
                    val starter = DfuServiceInitiator(it.address)
                    starter.setZip(R.raw.satellite_online_16)
                    starter.start(context, EltaDfuService::class.java)
                }
            }
            .retry()
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.DeviceClicked>()
            .doOnNext { click ->
                if (click.item.address != device?.address) {
                    device = scanResults.map { it.device }.firstOrNull { it.address == click.item.address }
                    val newItems = items.value.map { (it as DeviceItem).copy(isSelected = !it.isSelected) }
                    items.consumer.accept(newItems)
                }
            }
            .subscribe()
            .untilDestroy()

        DfuServiceListenerHelper.registerProgressListener(context, dfuListener)

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (!adapter.isEnabled) {
            adapter.enable()
        } else {
            startScanAction.consumer.accept(Unit)
        }

        requestPermissionsCommand.consumer.accept(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        DfuServiceListenerHelper.unregisterProgressListener(context, dfuListener)
    }
}