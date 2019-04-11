package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.nullgr.core.date.toDate
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("MagicNumber")
@Singleton
open class DefaultGlucometerEventBuilder @Inject constructor(
    private val generator: GlucometerEventIdGenerator
) : GlucometerEventBuilder {

    override fun buildFrom(glucometerId: String, response: String): GlucometerEventDto {
        val tokens = response.split(".")
        val dateToken = tokens[1]
        val temperatureAndValueToken = tokens[2]

        return GlucometerEventDto(
            id = generator.generate(glucometerId, dateToken),
            date = extractDate(dateToken),
            temperature = extractTemperature(temperatureAndValueToken),
            value = extractValue(temperatureAndValueToken)
        )
    }

    protected open fun extractDate(token: String): Date? = "20$token".toDate("yyyyMMddHHmm")

    protected open fun extractTemperature(token: String): Int? = token.substring(0, 3).toInt()

    protected open fun extractValue(token: String): Double? = token.substring(3, 6).toDouble().div(10)
}