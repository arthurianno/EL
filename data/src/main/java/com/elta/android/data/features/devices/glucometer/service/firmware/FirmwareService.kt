package com.elta.android.data.features.devices.glucometer.service.firmware

import android.content.Context
import android.os.Build
import com.elta.android.common.errors.FirmwareUpdateError
import com.elta.android.common.errors.GlucometerToDfuModeError
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.devices.glucometer.command.Commands
import com.elta.android.data.features.devices.glucometer.builder.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.storage.GlucometerPinStorage
import com.elta.android.data.features.devices.glucometer.service.UtilService
import com.elta.android.data.features.devices.glucometer.service.checkPinAndSend
import com.elta.android.data.features.devices.glucometer.service.connect.ConnectService
import com.elta.android.data.features.devices.glucometer.service.isOk
import com.elta.android.data.features.devices.glucometer.startScan
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import no.nordicsemi.android.dfu.DfuProgressListener
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirmwareService @Inject constructor(
    private val utilService: UtilService,
    private val connectService: ConnectService,

    private val glucometersInfoCache: Cache<GlucometerInfoCachedDto>,
    private val pinStorage: GlucometerPinStorage,
    private val infoBuilder: GlucometerInfoBuilder,
    private val context: Context
) {

    fun updateFirmware(address: String, file: FirmwareFile): Observable<String> =
        utilService.checkBluetoothClientState()
            .flatMap {
                connectService.findConnection(address)
                    .checkPinAndSend(pinStorage.getPin(address), request = { connection, pin ->
                        utilService.request(connection, address, Commands.SetPin(pin))
                    })
                    .switchMap { connection ->
                        utilService.request(connection, address, Commands.GetBatteryAndTemperature)
                            .map { infoBuilder.buildFrom(address, listOf(it)) }
                            .switchMap { info ->
                                utilService.checkBattery(info, connection, address)
                            }
                    }
                    .take(1)
                    .switchMap { response ->
                        firmwareUpdate(response, address, file)
                    }
                    // we can't know when device will completely reboot after update
                    // to get actual info so we using this this hack to update glucometer
                    // version after update firmware.
                    .doOnComplete {
                        val id = address.hashCode().toLong()
                        glucometersInfoCache.get(CommonConditions.ById(id))?.let { info ->
                            glucometersInfoCache.update(
                                listOf(info.copy(software = file.version))
                            )
                        }
                    }
            }

    private fun firmwareUpdate(
        response: String,
        address: String,
        file: FirmwareFile
    ): Observable<String> {
        return if (response.isOk()) {
            val dfuAddress = address.toDfuAddress()
            utilService.scanner.startScan(utilService.dfuFilters, utilService.settings, context)
                .filter { results -> results.map { it.device.address }.contains(dfuAddress) }
                .take(1)
                .switchMap { startFirmwareUpdate(context, file.path, dfuAddress) }
        } else {
            Observable.error(GlucometerToDfuModeError)
        }
    }

    private fun startFirmwareUpdate(
        context: Context,
        path: String,
        deviceAddress: String
    ): Observable<String> = Observable.create { emitter ->

        val listener = object : DfuProgressLogger() {
            override fun onProgressChanged(
                address: String,
                percent: Int,
                speed: Float,
                avgSpeed: Float,
                currentPart: Int,
                partsTotal: Int
            ) {
                emitter.onNext("speed=$speed, progress=$percent")
            }

            override fun onDfuCompleted(address: String) {
                super.onDfuCompleted(address)
                if (!emitter.isDisposed && address == deviceAddress) {
                    emitter.onComplete()
                }
            }

            override fun onError(address: String, error: Int, errorType: Int, message: String) {
                super.onError(address, error, errorType, message)
                if (!emitter.isDisposed && address == deviceAddress) {
                    emitter.onError(FirmwareUpdateError(message))
                }
            }
        }

        DfuServiceListenerHelper.registerProgressListener(context, listener)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(context)
        }

        val starter = DfuServiceInitiator(deviceAddress).apply {
            setDeviceName("Dfu")
            setKeepBond(false)
            setForceDfu(true)
            setForceScanningForNewAddressInLegacyDfu(false)
            setPrepareDataObjectDelay(400L)
            setRebootTime(0)
            setScanTimeout(2000)
            setZip(path)
        }

        starter.start(context, EltaDfuService::class.java)

        emitter.setDisposable(
            Disposables.fromAction {
                DfuServiceListenerHelper.unregisterProgressListener(context, listener)
            }
        )
    }

    @Suppress("MagicNumber")
    private fun String.toDfuAddress(): String {
        val tokens = this.split(":")
        val token = tokens.last()
        val hex = BigInteger(token, 16)
        val new = hex.plus(BigInteger.ONE).toString(16).padStart(2, '0').takeLast(2)

        return tokens.joinToString(
            separator = ":",
            limit = tokens.size - 1,
            postfix = new,
            truncated = ""
        ).uppercase()
    }


    @Suppress("MaxLineLength")
    abstract class DfuProgressLogger : DfuProgressListener {

        override fun onProgressChanged(
            address: String,
            percent: Int,
            speed: Float,
            avgSpeed: Float,
            currentPart: Int,
            partsTotal: Int
        ) {
            Timber.i("onProgressChanged, address=$address, percent=$percent, speed=$speed, avgSpeed=$avgSpeed, currentPart=$currentPart, partsTotal=$partsTotal")
        }

        override fun onDeviceDisconnecting(address: String) {
            Timber.i("onDeviceDisconnecting, address=$address")
        }

        override fun onDeviceDisconnected(address: String) {
            Timber.i("onDeviceDisconnected, address=$address")
        }

        override fun onDeviceConnected(address: String) {
            Timber.i("onDeviceConnected, address=$address")
        }

        override fun onDfuProcessStarting(address: String) {
            Timber.i("onDfuProcessStarting, address=$address")
        }

        override fun onDfuAborted(address: String) {
            Timber.i("onDfuAborted, address=$address")
        }

        override fun onEnablingDfuMode(address: String) {
            Timber.i("onEnablingDfuMode, address=$address")
        }

        override fun onDfuCompleted(address: String) {
            Timber.i("onDfuCompleted, address=$address")
        }

        override fun onFirmwareValidating(address: String) {
            Timber.i("onFirmwareValidating, address=$address")
        }

        override fun onDfuProcessStarted(address: String) {
            Timber.i("onDfuProcessStarted, address=$address")
        }

        override fun onError(address: String, error: Int, errorType: Int, message: String) {
            Timber.i("onError, address=$address, error=$error, errorType=$errorType, message=$message")
        }

        override fun onDeviceConnecting(address: String) {
            Timber.i("onDeviceDisconnecting, address=$address")
        }
    }


}