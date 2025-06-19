package com.elta.android.domain.features.devices.repository

interface BluetoothStateRepository {

    fun isBluetoothPermissionGranted() : Boolean

    fun isLocationPermissionGranted() : Boolean

    fun isBluetoothEnabled() : Boolean

    fun isLocationEnabled(): Boolean
}
