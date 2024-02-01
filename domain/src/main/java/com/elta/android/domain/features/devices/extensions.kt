package com.elta.android.domain.features.devices

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import kotlin.jvm.Throws

@Throws(LocationPermissionNotGrantedError::class, BluetoothNotEnabledError::class)
fun BluetoothStateRepository.checkBluetoothAvailabilityAndPermissions() {
    when {
        !isPermissionGranted() -> {
            throw LocationPermissionNotGrantedError
        }
        !isBluetoothEnable() -> {
            throw BluetoothNotEnabledError
        }
    }
}