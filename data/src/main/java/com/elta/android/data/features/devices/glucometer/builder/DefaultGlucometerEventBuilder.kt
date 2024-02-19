package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.fromGlucometerDateTime
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.user.interactor.round
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeParseException
import timber.log.Timber
import java.util.Date
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
    ): GlucometerEvent {
        val tokens = getTokens(response)
        val dateToken = tokens.first
        val temperatureAndValueToken = tokens.second

        return GlucometerEvent(
            id = generator.generate(userId, glucometerId, dateToken),
            date = extractDate(dateToken),
            temperature = extractTemperature(temperatureAndValueToken),
            value = extractValue(temperatureAndValueToken),
            glucometerSerialNumber = glucometerSerialNumber,
            originalResponse = response
        )
    }

    override fun getDate(response: String): Date {
        val dateFromGlucometer = getTokens(response).first
        val mills = extractDate(dateFromGlucometer)?.toInstant()?.toEpochMilli() ?: 0L
        return Date(mills)
    }

    override fun getValue(response: String): Double {
        val token = getTokens(response)
        return extractValue(token.second) ?: 0.0
    }

    private fun getTokens(response: String): Pair<String, String> {
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
