package com.elta.android.domain.features.devices.repository

// fixme Variant A : improved_enabling_location
interface BluetoothStateRepositoryVariantA {

    fun isPermissionGranted() : Boolean

    fun isBluetoothEnabled() : Boolean

    fun isLocationEnabledPre34Api(): Boolean
}
