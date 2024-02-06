package com.elta.android.data.features.devices.glucometer.firmware

import android.content.Context
import android.os.Build
import com.elta.android.common.errors.FirmwareUpdateError
import com.elta.android.data.features.devices.glucometer.firmware.utils.DfuProgressLogger
import com.elta.android.data.features.devices.glucometer.service.firmware.EltaDfuService
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirmwareManagerImpl @Inject constructor(
    private val context: Context
): FirmwareManager {
    override suspend fun updateFirmware(address: String, filePath: String): String {
        return suspendCoroutine { continuation ->
            val listener = object : DfuProgressLogger() {
                override fun onDfuCompleted(address: String) {
                    super.onDfuCompleted(address)
                    continuation.resume("Dfu update completed")
                }

                override fun onError(address: String, error: Int, errorType: Int, message: String) {
                    super.onError(address, error, errorType, message)
                    continuation.resumeWithException(FirmwareUpdateError(message))
                }
            }

            DfuServiceListenerHelper.registerProgressListener(context, listener)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DfuServiceInitiator.createDfuNotificationChannel(context)
            }

            val starter = DfuServiceInitiator(address).apply {
                setDeviceName("Dfu")
                setKeepBond(false)
                setForceDfu(true)
                setForceScanningForNewAddressInLegacyDfu(false)
                setPrepareDataObjectDelay(400L)
                setRebootTime(0)
                setScanTimeout(2000)
                setZip(filePath)
            }

            starter.start(context, EltaDfuService::class.java)
        }
    }
}
