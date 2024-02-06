package com.elta.android.data.features.devices.glucometer.firmware.utils

import no.nordicsemi.android.dfu.DfuProgressListener
import timber.log.Timber

abstract class DfuProgressLogger : DfuProgressListener {

    override fun onProgressChanged(
        address: String,
        percent: Int,
        speed: Float,
        avgSpeed: Float,
        currentPart: Int,
        partsTotal: Int
    ) {
        Timber.i(
            "onProgressChanged, address=$address, percent=$percent, " +
                    "speed=$speed, avgSpeed=$avgSpeed, currentPart=$currentPart, partsTotal=$partsTotal"
        )
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
