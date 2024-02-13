package com.elta.android.domain.features.devices.repository

interface BluetoothStateRepository {

    fun isPermissionGranted() : Boolean

    fun isBluetoothEnabled() : Boolean

    fun isLocationEnabledPre34Api(): Boolean
}
