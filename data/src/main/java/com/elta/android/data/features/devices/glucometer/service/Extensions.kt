package com.elta.android.data.features.devices.glucometer.service

internal fun String?.isPinOk(): Boolean = this == "pin.ok"
internal fun String.isEmptyEvent(): Boolean = contains("rd000000000000000000")
internal fun String.isEmptyMemoryEvent(): Boolean = equals("mem.empty", ignoreCase = true)
internal fun String.isOk(): Boolean = endsWith("ok")
