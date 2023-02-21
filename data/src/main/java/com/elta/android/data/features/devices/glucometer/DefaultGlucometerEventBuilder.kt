package com.elta.android.data.features.devices.glucometer

import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.domain.features.user.interactor.round
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeParseException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("MagicNumber")
@Singleton
open class DefaultGlucometerEventBuilder @Inject constructor(
    private val generator: GlucometerEventIdGenerator
) : GlucometerEventBuilder {

    override fun buildFrom(
        userId: String,
        glucometerId: String,
        response: String,
        glucometerSerialNumber: String?
    ): GlucometerEventDto {
        val tokens = getTokens(response)
        val dateToken = tokens.first
        val temperatureAndValueToken = tokens.second

        Timber.i("<<<<<<< DefaultGlucometerEventBuilder >>>>>>  Response : $response")
        Timber.i("<<<<<<< DefaultGlucometerEventBuilder >>>>>>  Tokens : $tokens")
        Timber.i("<<<<<<< DefaultGlucometerEventBuilder >>>>>>  Date : ${extractDate(dateToken)}")
        Timber.i(
            "<<<<<<< DefaultGlucometerEventBuilder >>>>>>  Temperature : ${
                extractTemperature(temperatureAndValueToken)
            }"
        )
        Timber.i(
            "<<<<<<< DefaultGlucometerEventBuilder >>>>>>  Glucose Value : ${
                extractValue(
                    temperatureAndValueToken
                )
            }"
        )

        return GlucometerEventDto(
            id = generator.generate(userId, glucometerId, dateToken),
            date = extractDate(dateToken),
            temperature = extractTemperature(temperatureAndValueToken),
            value = extractValue(temperatureAndValueToken),
            glucometerSerialNumber = glucometerSerialNumber
        )
    }

    protected open fun getTokens(response: String): Pair<String, String> {
        val cleaned = response.replace("rd", "")
        val dateToken = cleaned.substring(0, 12)
        val temperatureAndValueToken = cleaned.substring(12, cleaned.length)
        return Pair(dateToken, temperatureAndValueToken)
    }

    protected open fun extractDate(token: String): ZonedDateTime? {
        return try {
            token.fromGlucometerDateTime()
        } catch (ex: DateTimeParseException) {
            Timber.e(ex)
            ZonedDateTime.now()
        }
    }

    protected open fun extractTemperature(token: String): Double? =
        (token.substring(0, 3).toDouble() / 10).round(1)

    protected open fun extractValue(token: String): Double? =
        token.substring(3, 6).toDouble().div(10)
}
