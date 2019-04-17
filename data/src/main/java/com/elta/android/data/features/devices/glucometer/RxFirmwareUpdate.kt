package com.elta.android.data.features.devices.glucometer

import android.content.Context
import android.os.Build
import com.elta.android.common.errors.FirmwareUpdateError
import io.reactivex.Completable
import io.reactivex.disposables.Disposables
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper

fun startFirmwareUpdate(context: Context, path: String, address: String): Completable = Completable.create { emitter ->

    val listener = object : DfuProgressListenerAdapter() {
        override fun onDfuCompleted(deviceAddress: String) {
            if (!emitter.isDisposed && deviceAddress == address) {
                emitter.onComplete()
            }
        }

        override fun onError(deviceAddress: String?, error: Int, errorType: Int, message: String) {
            if (!emitter.isDisposed && deviceAddress == address) {
                emitter.onError(FirmwareUpdateError(message))
            }
        }
    }

    DfuServiceListenerHelper.registerProgressListener(context, listener)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DfuServiceInitiator.createDfuNotificationChannel(context)
    }
    val starter = DfuServiceInitiator(address)
    starter.setZip(path)
    starter.start(context, EltaDfuService::class.java)

    emitter.setDisposable(Disposables.fromAction {
        DfuServiceListenerHelper.unregisterProgressListener(context, listener)
    })
}