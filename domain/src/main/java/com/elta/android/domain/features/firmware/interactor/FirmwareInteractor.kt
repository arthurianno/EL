package com.elta.android.domain.features.firmware.interactor

import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.Firmware

const val FIRMWARE_VERSION = "1.6" // version of firmware supported by application
const val MIN_LEVEL = 1 // minimal level of battery required to start firmware update

fun Firmware.isSupportedByApplication(): Boolean {
    val appVersionCode = FIRMWARE_VERSION.replace(".", "").toInt()
    val compatibleVersionCode = compatible.replace(".", "").toInt()
    return appVersionCode >= compatibleVersionCode
}

fun GlucometerInfo.isBatteryLevelEnoughForUpdate(): Boolean = batteryLevel ?: 0 >= MIN_LEVEL