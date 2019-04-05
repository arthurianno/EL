package com.elta.android.data.features.devices.dto

import android.bluetooth.BluetoothDevice

data class GlucometerDto(
    val id: String,
    val address: String,
    val name: String?,
    val device: BluetoothDevice
)