package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.Firmware

private val pinRegex = Regex("^[0-9]{3}")

fun isPinValid(pin: String) = pin.matches(pinRegex)

fun GlucometerInfo.isFirmwareNewer(firmware: Firmware): Boolean {
    val deviceVersionString = softwareVersion.toString()
    val deviceVersion = deviceVersionString.replace(".", "").toInt()
    val newVersion = firmware.version.replace(".", "").toInt()
    return newVersion > deviceVersion
}
