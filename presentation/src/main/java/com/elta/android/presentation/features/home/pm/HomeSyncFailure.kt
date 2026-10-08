package com.elta.android.presentation.features.home.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.BluetoothPermissionNotGrantedError
import com.elta.android.common.errors.BluetoothScannerError
import com.elta.android.common.errors.CommandError
import com.elta.android.common.errors.GlucometerConnectionException
import com.elta.android.common.errors.GlucometerDeviceHardwareError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import java.util.concurrent.TimeoutException

internal enum class ManualSyncFailure {
    CONNECTION_DISABLED,
    DEVICE_MISSING,
    PERMISSION_DENIED,
    DEVICE_OFFLINE,
    HARDWARE,
    DEVICE_TIMEOUT,
    DEVICE_ERROR,
    UNEXPECTED
}

internal fun classifyManualSyncFailure(error: Throwable): ManualSyncFailure = when (error) {
    is BluetoothNotEnabledError, is LocationNotEnabledError -> ManualSyncFailure.CONNECTION_DISABLED
    is PrimaryGlucometerNotFoundError -> ManualSyncFailure.DEVICE_MISSING
    is LocationPermissionNotGrantedError, is BluetoothPermissionNotGrantedError ->
        ManualSyncFailure.PERMISSION_DENIED
    is GlucometerOfflineError -> ManualSyncFailure.DEVICE_OFFLINE
    is GlucometerDeviceHardwareError -> ManualSyncFailure.HARDWARE
    is GlucometerSyncError, is CommandError, is GlucometerConnectionException -> when (error.cause) {
        is GlucometerDeviceHardwareError -> ManualSyncFailure.HARDWARE
        is TimeoutException -> ManualSyncFailure.DEVICE_TIMEOUT
        else -> ManualSyncFailure.DEVICE_ERROR
    }
    else -> ManualSyncFailure.UNEXPECTED
}

internal enum class AutoSyncFailure { SILENT, MESSAGE, DEVICE_MISSING, UNEXPECTED }

internal fun classifyAutoSyncFailure(error: Throwable): AutoSyncFailure = when {
    error is BluetoothNotEnabledError || error is LocationNotEnabledError ||
        error.cause is BluetoothNotEnabledError || error.cause is LocationNotEnabledError ||
        error is LocationPermissionNotGrantedError || error is BluetoothPermissionNotGrantedError ||
        error is CommandError || error is BluetoothScannerError -> AutoSyncFailure.SILENT
    error is GlucometerSyncError &&
        (error.cause is GlucometerOfflineError || error.cause is TimeoutException) ||
        error is GlucometerConnectionException -> AutoSyncFailure.MESSAGE
    error is PrimaryGlucometerNotFoundError -> AutoSyncFailure.DEVICE_MISSING
    else -> AutoSyncFailure.UNEXPECTED
}
