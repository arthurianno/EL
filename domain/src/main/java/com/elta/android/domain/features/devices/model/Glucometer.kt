package com.elta.android.domain.features.devices.model

import android.bluetooth.BluetoothDevice

data class Glucometer(
    val id: String,
    val address: String,
    val name: String?,
    val device: BluetoothDevice
)