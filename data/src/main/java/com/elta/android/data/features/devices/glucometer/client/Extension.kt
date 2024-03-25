package com.elta.android.data.features.devices.glucometer.client

import com.elta.android.common.utils.toLocalDateTime
import com.elta.android.data.features.devices.dto.VersionDto
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeParseException
import timber.log.Timber

private const val DOT_SYMBOL = '.'
private const val SPACE_SYMBOL = ' '
private const val BATTERY_PREFIX = "b"
private const val ZERO_CHAR = '0'
private const val TEMPERATURE_PREFIX = "t"
private const val SOFT_VERSION_PREFIX = "sw:"
private const val HARD_VERSION_PREFIX = "hw:"
private const val TIME_PREFIX = "time"
private const val SERIAL_PREFIX = "ser"
private const val DATE_TIME_PATTERN = "yyyyMMddHHmmss"

internal fun String.extractDate(): LocalDateTime {
    return try {
        "20${split(DOT_SYMBOL).component2()}".toLocalDateTime(DATE_TIME_PATTERN)
    } catch (ex: DateTimeParseException) {
        Timber.e(ex)
        LocalDateTime.now()
    }
}

internal fun String.extractVersion(): VersionDto =
    with(split(SPACE_SYMBOL)) {
        VersionDto(
            software = component2().removePrefix(SOFT_VERSION_PREFIX),
            hardware = component1().removePrefix(HARD_VERSION_PREFIX)
        )
    }

internal fun String.extractBatteryAndTemperature(): Pair<Int, Int> =
    with(split(DOT_SYMBOL)) {
        Pair(
            component1().removePrefix(BATTERY_PREFIX).toInt(),
            component2().removePrefix(TEMPERATURE_PREFIX).replace(SPACE_SYMBOL, ZERO_CHAR).toInt()
        )
    }

internal fun String.extractSerial(): String = split(DOT_SYMBOL).last()