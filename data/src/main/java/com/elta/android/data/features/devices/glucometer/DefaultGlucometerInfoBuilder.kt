package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import com.nullgr.core.date.toDate
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class DefaultGlucometerInfoBuilder @Inject constructor() : GlucometerInfoBuilder {

    override fun buildFrom(params: List<String>, syncDate: Date?): GlucometerInfoDto {
        var date: Date? = null
        var temperature: Int? = null
        var batteryLevel: Int? = null
        var version: VersionDto? = null

        params.forEach { param ->
            when {
                param.startsWith("time") -> date = extractDate(param)
                param.startsWith("soft") -> version = extractVersion(param)
                param.startsWith("b") -> {
                    val response = extractBatteryAndTemperature(param)
                    batteryLevel = response.first
                    temperature = response.second
                }
            }
        }

        return GlucometerInfoDto(
            deviceDate = date,
            syncDate = syncDate,
            temperature = temperature,
            batteryLevel = batteryLevel,
            version = version
        )
    }

    protected open fun extractDate(param: String): Date? {
        val payload = param.split(".")[1]
        return "20$payload".toDate("yyyyMMddHHmmss")
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
        val temperature = tokens[1].removePrefix("t").toInt()
        return Pair(battery, temperature)
    }
}