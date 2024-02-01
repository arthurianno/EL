package com.elta.android.domain.features.devices.repository

interface BluetoothStateRepository {

    fun isPermissionGranted() : Boolean

    fun isBluetoothEnable() : Boolean
}
