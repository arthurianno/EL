package com.elta.android.data.features.devices.glucometer.client

import com.elta.android.common.utils.toLocalDateTime
import com.elta.android.data.features.devices.dto.VersionDto
import org.threeten.bp.LocalDateTime

private const val DOT_SYMBOL = '.'
private const val SPACE_SYMBOL = ' '
private const val HEX_RADIX = 16
private const val DATE_TIME_PATTERN = "yyyyMMddHHmmss"

internal fun String.extractDate(): LocalDateTime {
    return try {
        "20${split(DOT_SYMBOL).component2()}".toLocalDateTime(DATE_TIME_PATTERN)
    } catch (ex: Exception) {
        throw IllegalArgumentException("Invalid time payload: $this", ex)
    }
}

internal fun String.extractVersion(): VersionDto {
    val match = checkNotNull(VERSION_REGEX.find(this)) {
        "Invalid version payload: $this"
    }
    return VersionDto(
        software = match.groupValues[2],
        hardware = match.groupValues[1]
    )
}

internal fun String.extractBatteryAndTemperature(): Pair<Int, Int> {
    val normalized = lowercase().replace(SPACE_SYMBOL.toString(), "")
    val match = checkNotNull(BATTERY_REGEX.find(normalized)) {
        "Invalid battery payload: $this"
    }
    return match.groupValues[1].toInt() to match.groupValues[2].toInt()
}

internal fun String.extractSerial(): String {
    val match = checkNotNull(SERIAL_REGEX.find(this)) {
        "Invalid serial payload: $this"
    }
    return match.groupValues[1]
}

internal fun String.extractZoneOffsetSeconds(): Int {
    val match = checkNotNull(ZONE_REGEX.find(this)) {
        "Invalid zone payload: $this"
    }
    return match.groupValues[1].toLong(HEX_RADIX).toInt()
}

internal fun String.extractErrorWord(): Long {
    val match = checkNotNull(ERROR_REGEX.find(this)) {
        "Invalid error payload: $this"
    }
    return match.groupValues[1].toLong(HEX_RADIX)
}

internal fun Int.toZoneHexString(): String = toUInt().toString(HEX_RADIX).uppercase().padStart(8, '0')

private val VERSION_REGEX = Regex("""hw:([^\s]+)\s+sw:([^\s]+)""", RegexOption.IGNORE_CASE)
private val BATTERY_REGEX =
    Regex("""b(\d)(?:\.)?t(\d{1,3})""", RegexOption.IGNORE_CASE)
private val SERIAL_REGEX = Regex("""ser\.([A-Za-z0-9]{11})$""", RegexOption.IGNORE_CASE)
private val ZONE_REGEX = Regex("""zone\.([0-9A-Fa-f]{8})$""")
private val ERROR_REGEX = Regex("""error\.([0-9A-Fa-f]{8})$""", RegexOption.IGNORE_CASE)

