@file:Suppress("VariableNaming", "MaxLineLength", "MagicNumber", "LongMethod")

package com.elta.android.presentation.features.bluetooth.pm

import android.content.Context
import android.os.Build
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerPinIncorrectOrNotFoundError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.interactor.FindGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometerEventsUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometerInfoUseCase
import com.elta.android.domain.features.devices.interactor.SetPinCodeUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.bluetooth.EltaDfuService
import com.elta.android.presentation.features.bluetooth.ui.adapter.items.DeviceItem
import com.elta.android.presentation.messages.SnackBarMessageData
import io.reactivex.Observable
import me.dmdev.rxpm.widget.inputControl
import no.nordicsemi.android.dfu.DfuProgressListener
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import timber.log.Timber
import javax.inject.Inject

class BluetoothPm @Inject constructor(
    private val setPinCodeUseCase: SetPinCodeUseCase,
    private val getGlucometerEventsUseCase: GetGlucometerEventsUseCase,
    private val getGlucometerInfoUseCase: GetGlucometerInfoUseCase,
    private val findGlucometersUseCase: FindGlucometersUseCase,
    private val context: Context,
    services: ServiceFacade
) : BaseListPm(services) {

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

    val getInfoAction = Action<Unit>()
    val getEventsAction = Action<Unit>()
    val setPinAction = Action<Unit>()
    val pinEnabledState = State(false)
    val dfuAction = Action<Unit>()

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

        dfuAction.observable
            .doOnNext {
                glucometer?.let {
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
                if (click.item.address != glucometer?.address) {
                    glucometer = scanResults.firstOrNull { it.address == click.item.address }
                    val newItems = items.value.map { (it as DeviceItem).copy(isSelected = it.address == click.item.address) }
                    items.consumer.accept(newItems)
                }
            }
            .subscribe()
            .untilDestroy()

        pinInputControl.textChanges.observable
            .map { it.length == 3 }
            .subscribe(pinEnabledState.consumer)
            .untilDestroy()

        DfuServiceListenerHelper.registerProgressListener(context, dfuListener)

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
            bluetoothEnabledAction.observable,
            locationPermissionsGrantedAction.observable,
            locationEnabledAction.observable
        )
            .subscribe(startScanAction.consumer)
            .untilDestroy()
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
            is GlucometerPinIncorrectOrNotFoundError -> showSnackBarCommand.consumer.accept(SnackBarMessageData.SimpleTextMessage("Enter pin code at input field and press SET PIN"))
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
}