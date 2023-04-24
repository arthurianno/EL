package com.elta.android.domain.features.devices.interactor

import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.Firmware

private const val DOT_CHAR = "."
private const val ZERO_CHAR = "0"
private const val MIN_VERSION_NUMBER_COUNT = 2
private val pinRegex = Regex("^[0-9]{3}")

fun isPinValid(pin: String) = pin.matches(pinRegex)

fun GlucometerInfo.isFirmwareNewer(firmware: Firmware): Boolean = runCatching {
    firmware.version.splitVersionNumber() > softwareVersion.orEmpty().splitVersionNumber()
}.getOrDefault(false)

private fun String.splitVersionNumber(): Int =
    this.split(DOT_CHAR)
        .toMutableList()
        .apply { if (count() == MIN_VERSION_NUMBER_COUNT) add(ZERO_CHAR) }
        .joinToString("")
        .toInt()
