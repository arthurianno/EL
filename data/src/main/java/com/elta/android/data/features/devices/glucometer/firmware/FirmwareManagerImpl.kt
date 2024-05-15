package com.elta.android.data.features.devices.glucometer.firmware

import android.content.Context
import android.content.Intent
import android.os.Build
import com.elta.android.common.errors.FirmwareUpdateError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.features.devices.glucometer.firmware.utils.DfuProgressLogger
import com.elta.android.data.features.devices.glucometer.service.firmware.BootModeService
import com.elta.android.data.features.devices.glucometer.service.firmware.EltaDfuService
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirmwareManagerImpl @Inject constructor(
    private val context: Context,
    private val crashlyticsReport: CrashlyticsReport
) : FirmwareManager {
    override suspend fun updateFirmwareWithNordicDfu(address: String, filePath: String): String {
        return suspendCoroutine { continuation ->
            val listener = object : DfuProgressLogger() {
                override fun onDfuCompleted(address: String) {
                    super.onDfuCompleted(address)
                    crashlyticsReport.log("Firmware update completed")
                    continuation.resume("Dfu update completed")
                }

                override fun onError(address: String, error: Int, errorType: Int, message: String) {
                    super.onError(address, error, errorType, message)
                    val error = FirmwareUpdateError(message)
                    crashlyticsReport.writeException(error)
                    continuation.resumeWithException(error)
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

            crashlyticsReport.log("The device firmware update has started")
            starter.start(context, EltaDfuService::class.java)
        }
    }

    override suspend fun updateFirmwareWithBootMode(
        address: String,
        pin: String,
        filePath: String
    ) {
        val intent = Intent(context, BootModeService::class.java).apply {
            putExtra(BootModeService.ADDRESS_EXTRA_KEY, address)
            putExtra(BootModeService.PIN_EXTRA_KEY, pin)
            putExtra(BootModeService.FILE_PATH_EXTRA_KEY, filePath)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
