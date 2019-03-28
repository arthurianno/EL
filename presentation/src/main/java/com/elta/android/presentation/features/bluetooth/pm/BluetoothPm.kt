@file:Suppress("VariableNaming", "MaxLineLength", "MagicNumber", "LongMethod")

package com.elta.android.presentation.features.bluetooth.pm

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.bluetooth.EltaDfuService
import com.elta.android.presentation.features.bluetooth.ui.adapter.items.DeviceItem
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import me.dmdev.rxpm.widget.inputControl
import no.nordicsemi.android.dfu.DfuProgressListener
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import timber.log.Timber
import java.nio.charset.Charset
import java.util.UUID
import javax.inject.Inject

class BluetoothPm @Inject constructor(
    private val findGlucometersUseCase: FindGlucometersUseCase,
    private val context: Context,
    private val client: RxBleClient,
    services: ServiceFacade
) : BaseListPm(services) {

    private val UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")

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

    private val scanResults = mutableSetOf<Glucometer>()
    private val connectionObservable = State<RxBleConnection>()
    val commandInputControl = inputControl()
    val logState = State("Log:")
    val requestEnableBluetoothCommand = Command<Unit>(bufferSize = 1)
    val requestLocationPermissionsCommand = Command<Unit>(bufferSize = 1)
    val requestEnableLocationCommand = Command<Unit>(bufferSize = 1)
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
                            results.map { glucometer ->
                                DeviceItem(
                                    id = glucometer.id,
                                    name = glucometer.name ?: "Unknown device",
                                    address = glucometer.address,
                                    isSelected = glucometer.address == device?.address
                                )
                            }
                        )
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

//        client.observeStateChanges()
//            .log("Bluetooth", "state") { it.name }
//            .doOnNext { state ->
//                when (state) {
//                    RxBleClient.State.READY -> startScanAction.consumer.accept(Unit)
//                    else -> {
//                    }
//                }
//            }
//            .retry()
//            .subscribe()
//            .untilDestroy()

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

//        bus.clicks<Clicks.DeviceClicked>()
//            .doOnNext { click ->
//                if (click.item.address != device?.address) {
//                    device = scanResults.map { it.device }.firstOrNull { it.address == click.item.address }
//                    val newItems = items.value.map { (it as DeviceItem).copy(isSelected = !it.isSelected) }
//                    items.consumer.accept(newItems)
//                }
//            }
//            .subscribe()
//            .untilDestroy()

        DfuServiceListenerHelper.registerProgressListener(context, dfuListener)

//        val adapter = BluetoothAdapter.getDefaultAdapter()
//        if (!adapter.isEnabled) {
//            adapter.enable()
//        } else {
//            startScanAction.consumer.accept(Unit)
//        }

        startScanAction.consumer.accept(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        DfuServiceListenerHelper.unregisterProgressListener(context, dfuListener)
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is BluetoothNotEnabledError -> requestEnableBluetoothCommand.consumer.accept(Unit)
            is LocationPermissionNotGrantedError -> requestLocationPermissionsCommand.consumer.accept(Unit)
            is LocationNotEnabledError -> requestEnableLocationCommand.consumer.accept(Unit)
            else -> super.handleError(error)
        }
    }
}