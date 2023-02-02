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

private const val RESPONSE_TIME = "time"
private const val RESPONSE_HW_VERSION = "hw:"
private const val RESPONSE_SW_VERSION = "sw:"
private const val RESPONSE_BATTERY = "b"
private const val DATETIME_PATTERN = "yyyyMMddHHmmss"
private const val CENTURY = "20"
private const val DELIMITER_SPACE = " "
private const val DELIMITER_DOT = "."

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

        params.forEach { param ->
            when {
                param.startsWith(RESPONSE_TIME) -> date = ZonedDateTime.of(extractDate(param), ZoneId.systemDefault())
                param.startsWith(RESPONSE_HW_VERSION) -> version = extractVersion(param)
                param.startsWith(RESPONSE_BATTERY) -> {
                    val response = extractBatteryAndTemperature(param)
                    batteryLevel = response.first
                    temperature = response.second
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
            lastSyncedEvent = lastSyncedEvent
        )
    }

    protected open fun extractDate(param: String): LocalDateTime? {
        return try {
            val glucometerDate = param.split(DELIMITER_DOT)[1]
            "$CENTURY$glucometerDate".toLocalDateTime(DATETIME_PATTERN)
        } catch (parseException: DateTimeParseException) {
            Timber.e(parseException)
            LocalDateTime.now()
        }
    }

    protected open fun extractVersion(param: String): VersionDto {
        val tokens = param.split(DELIMITER_SPACE)
        val soft = tokens.component1().removePrefix(RESPONSE_HW_VERSION)
        val hard = tokens.component2().removePrefix(RESPONSE_SW_VERSION)
        return VersionDto(software = soft, hardware = hard)
    }

    protected open fun extractBatteryAndTemperature(param: String): Pair<Int, Int> {
        val tokens = param.split(DELIMITER_DOT)
        val battery = tokens.component1().removePrefix(RESPONSE_BATTERY).toInt()
        val temperature = tokens.component2().toInt()
        return Pair(battery, temperature)
    }
}
