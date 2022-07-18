package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerEventDto
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("MagicNumber")
@Singleton
open class DefaultGlucometerEventBuilder @Inject constructor(
    private val generator: GlucometerEventIdGenerator
) : GlucometerEventBuilder {

    override fun buildFrom(userId: String, glucometerId: String, response: String): GlucometerEventDto {
        val tokens = getTokens(response)
        val dateToken = tokens.first
        val temperatureAndValueToken = tokens.second

        Timber.i("<<<<<<< DefaultGlucometerEventBuilder >>>>>>  Response : $response")
        Timber.i("<<<<<<< DefaultGlucometerEventBuilder >>>>>>  Tokens : $tokens")
        Timber.i("<<<<<<< DefaultGlucometerEventBuilder >>>>>>  Value : ${extractValue(temperatureAndValueToken)}")

        return GlucometerEventDto(
            id = generator.generate(userId, glucometerId, dateToken),
            date = extractDate(dateToken),
            temperature = extractTemperature(temperatureAndValueToken),
            value = extractValue(temperatureAndValueToken)
        )
    }

    protected open fun getTokens(response: String): Pair<String, String> {
        val cleaned = response.replace("rd", "")
        val dateToken = cleaned.substring(0, 12)
        val temperatureAndValueToken = cleaned.substring(12, cleaned.length)
        return Pair(dateToken, temperatureAndValueToken)
    }

    protected open fun extractDate(token: String): ZonedDateTime? =
        token.fromGlucometerDateTime()

    protected open fun extractTemperature(token: String): Int? = token.substring(0, 3).toInt()

    protected open fun extractValue(token: String): Double? = token.substring(3, 6).toDouble().div(10)
}
