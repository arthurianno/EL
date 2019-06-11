package com.elta.android.data.features.devices.glucometer

import android.content.Context
import android.os.Build
import com.elta.android.common.errors.FirmwareUpdateError
import io.reactivex.Completable
import io.reactivex.disposables.Disposables
import no.nordicsemi.android.dfu.DfuProgressListener
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import timber.log.Timber

fun startFirmwareUpdate(context: Context, path: String, deviceAddress: String): Completable = Completable.create { emitter ->

    val listener = object : DfuProgressLogger() {
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
    val starter = DfuServiceInitiator(deviceAddress)
    starter.setDeviceName("SatelliteOnline")
    starter.setZip(path)
    starter.setForceDfu(true)
    starter.start(context, EltaDfuService::class.java)

    emitter.setDisposable(Disposables.fromAction {
        DfuServiceListenerHelper.unregisterProgressListener(context, listener)
    })
}

abstract class DfuProgressLogger : DfuProgressListener {

    override fun onProgressChanged(address: String, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
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