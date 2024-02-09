package com.elta.android.domain.features.devices

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import com.elta.android.domain.features.devices.repository.DeviceRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeoutException
import kotlin.jvm.Throws

const val CONNECT_TIMEOUT: Long = 60_000
const val COMMAND_TIMEOUT = 30_000L
@Throws(LocationPermissionNotGrantedError::class, BluetoothNotEnabledError::class)
fun BluetoothStateRepository.checkBluetoothAvailabilityAndPermissions(crashlyticsReport: CrashlyticsReport?) {
    when {
        !isPermissionGranted() -> {
            crashlyticsReport?.writeException(LocationPermissionNotGrantedError)
            throw LocationPermissionNotGrantedError
        }
        !isBluetoothEnable() -> {
            crashlyticsReport?.writeException(BluetoothNotEnabledError)
            throw BluetoothNotEnabledError
        }
    }
}
@Throws(GlucometerSyncError::class)
suspend fun DeviceRepository.connectWithTimeout(address: String, pinCode: String, isDfuMode: Boolean = false, crashlyticsReport: CrashlyticsReport?) {
    try {
        withTimeout(CONNECT_TIMEOUT) {
            connectDevice(address, pinCode, isDfuMode)
        }
    } catch (e: TimeoutCancellationException) {
        val exception = GlucometerSyncError(TimeoutException("device $address connection timeout"))
        crashlyticsReport?.writeException(exception)
        throw exception
    }
}