package com.elta.android.domain.features.devices

import com.elta.android.common.errors.BluetoothNotEnabledErrorVariantA
import com.elta.android.common.errors.LocationNotEnabledErrorVariantA
import com.elta.android.common.errors.LocationPermissionNotGrantedErrorVariantA
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.repository.BluetoothStateRepositoryVariantA

// fixme Variant A : improved_enabling_location
// fixme  РАскомментить если пригодится
//const val CONNECT_TIMEOUT: Long = 60_000
//const val SEND_DATA_TIMEOUT: Long = 120_000
//const val COMMAND_TIMEOUT = 30_000L
@Throws(LocationPermissionNotGrantedErrorVariantA::class, BluetoothNotEnabledErrorVariantA::class, LocationNotEnabledErrorVariantA::class)
fun BluetoothStateRepositoryVariantA.checkBluetoothAvailabilityAndPermissions(crashlyticsReport: CrashlyticsReport?) {
    when {
        !isPermissionGranted() -> {
            crashlyticsReport?.writeException(LocationPermissionNotGrantedErrorVariantA)
            throw LocationPermissionNotGrantedErrorVariantA
        }
        !isBluetoothEnabled() -> {
            crashlyticsReport?.writeException(BluetoothNotEnabledErrorVariantA)
            throw BluetoothNotEnabledErrorVariantA
        }
        !isLocationEnabledPre34Api() -> {
            crashlyticsReport?.writeException(LocationNotEnabledErrorVariantA)
            throw  LocationNotEnabledErrorVariantA
        }
    }
}

// fixme Variant A : improved_enabling_location
// fixme  РАскомментить если пригодится
//@Throws(GlucometerSyncError::class)
//suspend fun DeviceRepository.connectWithTimeout(address: String, pinCode: String, crashlyticsReport: CrashlyticsReport?) {
//    try {
//        withTimeout(CONNECT_TIMEOUT) {
//            connectDevice(address, pinCode)
//        }
//    } catch (e: TimeoutCancellationException) {
//        val exception = GlucometerSyncError(TimeoutException("device ${address.hideMac()} connection timeout"))
//        crashlyticsReport?.writeException(exception)
//        throw exception
//    }
//}
