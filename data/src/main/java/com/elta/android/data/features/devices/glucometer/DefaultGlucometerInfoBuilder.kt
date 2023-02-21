package com.elta.android.data.features.devices.glucometer

import com.elta.android.common.utils.toLocalDateTime
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeParseException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val DOT_SYMBOL = '.'
private const val SPACE_SYMBOL = ' '
private const val BATTERY_PREFIX = "b"
private const val TEMPERATURE_PREFIX = "t"
private const val SOFT_VERSION_PREFIX = "sw:"
private const val HARD_VERSION_PREFIX = "hw:"
private const val TIME_PREFIX = "time"
private const val SERIAL_PREFIX = "ser"
private const val DATE_TIME_PATTERN = "yyyyMMddHHmmss"

@Singleton
open class DefaultGlucometerInfoBuilder @Inject constructor() : GlucometerInfoBuilder {

    override fun buildFrom(
        id: String,
        params: List<String>,
        syncDate: ZonedDateTime?,
        lastSyncedEvent: String?
    ): GlucometerInfoDto {
        var date: ZonedDateTime? = null
        var temperature: Int? = null
        var batteryLevel: Int? = null
        var version: VersionDto? = null
        var serial: String? = null

        params.forEach { param ->
            when {
                param.startsWith(TIME_PREFIX) ->
                    date = ZonedDateTime.of(param.extractDate(), ZoneId.systemDefault())

                param.startsWith(SOFT_VERSION_PREFIX) -> version = param.extractVersion()
                param.startsWith(SERIAL_PREFIX) -> serial = param.extractSerial()
                param.startsWith(BATTERY_PREFIX) -> {
                    with(param.extractBatteryAndTemperature()) {
                        batteryLevel = first
                        temperature = second
                    }
                }
            }
        }

        return GlucometerInfoDto(
            id = id,
            deviceDate = date,
            syncDate = syncDate,
            temperature = temperature,
            batteryLevel = batteryLevel,
            version = version,
            glucometerSerialNumber = serial,
            lastSyncedEvent = lastSyncedEvent
        )
    }

    private fun String.extractDate(): LocalDateTime {
        return try {
            "20${split(DOT_SYMBOL).component2()}".toLocalDateTime(DATE_TIME_PATTERN)
        } catch (ex: DateTimeParseException) {
            Timber.e(ex)
            LocalDateTime.now()
        }
    }

    private fun String.extractVersion(): VersionDto =
        with(split(SPACE_SYMBOL)) {
            VersionDto(
                software = component1().removePrefix(SOFT_VERSION_PREFIX),
                hardware = component2().removePrefix(HARD_VERSION_PREFIX)
            )
        }

    private fun String.extractBatteryAndTemperature(): Pair<Int, Int> =
        with(split(DOT_SYMBOL)) {
            Pair(
                component1().removePrefix(BATTERY_PREFIX).toInt(),
                component2().removePrefix(TEMPERATURE_PREFIX).toInt()
            )
        }

    private fun String.extractSerial(): String = split(DOT_SYMBOL).last()
}
