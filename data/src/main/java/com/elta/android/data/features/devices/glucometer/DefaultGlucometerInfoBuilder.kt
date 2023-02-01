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
                param.startsWith("time") -> date = ZonedDateTime.of(extractDate(param), ZoneId.systemDefault())
                param.startsWith("soft") -> version = extractVersion(param)
                param.startsWith("b") -> {
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
            val payload = param.split(".")[1]
            "20$payload".toLocalDateTime("yyyyMMddHHmmss")
        } catch (ex: DateTimeParseException) {
            Timber.e(ex)
            LocalDateTime.now()
        }
    }

    protected open fun extractVersion(param: String): VersionDto {
        val tokens = param.split(" ")
        val soft = tokens[0].removePrefix("soft").toDouble()
        val hard = tokens[1].removePrefix("hard").toDouble()
        return VersionDto(software = soft, hardware = hard)
    }

    protected open fun extractBatteryAndTemperature(param: String): Pair<Int, Int> {
        val tokens = param.split(".")
        val battery = tokens[0].removePrefix("b").toInt()
        val temperature = tokens[1].toInt()
        return Pair(battery, temperature)
    }
}
