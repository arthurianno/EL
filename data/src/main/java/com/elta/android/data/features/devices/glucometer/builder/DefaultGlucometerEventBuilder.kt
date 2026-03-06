package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.fromGlucometerDateTime
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.user.interactor.round
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeParseException
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
        glucometerSerialNumber: String?,
        @Suppress("UNUSED_PARAMETER") glucometerName: String?
    ): GlucometerEvent {
        val parsedMeasurement = parseMeasurement(response)

        return GlucometerEvent(
            id = generator.generate(userId, glucometerId, parsedMeasurement.idToken),
            date = parsedMeasurement.date,
            temperature = parsedMeasurement.temperature,
            value = parsedMeasurement.value,
            glucometerSerialNumber = glucometerSerialNumber,
            originalResponse = response,
            mealTag = parsedMeasurement.mealTag
        )
    }

    override fun getDate(response: String): Date {
        val mills = parseMeasurement(response).date?.toInstant()?.toEpochMilli() ?: 0L
        return Date(mills)
    }

    override fun getValue(response: String): Double {
        return parseMeasurement(response).value ?: 0.0
    }

    private fun parseMeasurement(response: String): ParsedMeasurement {
        val normalizedResponse = response.trim()
        return when {
            RD_EVENT_REGEX.matches(normalizedResponse) -> parseRdMeasurement(normalizedResponse)
            MEM_EVENT_REGEX.matches(normalizedResponse) -> parseMemMeasurement(normalizedResponse)
            normalizedResponse.equals(MEM_EMPTY_EVENT, ignoreCase = true) ->
                throw IllegalArgumentException("Measurement payload is empty: $response")
            else -> throw IllegalArgumentException("Unsupported measurement format: $response")
        }
    }

    private fun parseRdMeasurement(response: String): ParsedMeasurement {
        val match = checkNotNull(RD_EVENT_REGEX.matchEntire(response)) {
            "Invalid rd payload: $response"
        }

        val dateToken = match.groupValues[1]
        val rawTemperature = match.groupValues[2].toInt()
        val rawValue = match.groupValues[3].toInt()

        return ParsedMeasurement(
            idToken = dateToken,
            date = extractDate(dateToken),
            temperature = extractTemperature(rawTemperature),
            value = extractValue(rawValue),
            mealTag = extractMealTag(rawTemperature)
        )
    }

    private fun parseMemMeasurement(response: String): ParsedMeasurement {
        val match = checkNotNull(MEM_EVENT_REGEX.matchEntire(response)) {
            "Invalid mem payload: $response"
        }
        val unixHex = match.groupValues[1]
        val statusHex = match.groupValues[2]
        val glucoseHex = match.groupValues[3]

        val unixSeconds = unixHex.toLong(HEX_RADIX)
        val statusWord = statusHex.toInt(HEX_RADIX)
        val glucoseValue = glucoseHex.toInt(HEX_RADIX).toDouble() / TEN_DIVISOR

        return ParsedMeasurement(
            idToken = "$unixHex$glucoseHex",
            date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(unixSeconds), ZoneOffset.UTC),
            temperature = null,
            value = glucoseValue,
            mealTag = if (statusWord and MEM_AFTER_MEAL_BIT_MASK != 0) MealTag.AFTERMEAL else null
        )
    }

    protected open fun extractDate(token: String): ZonedDateTime {
        return try {
            token.fromGlucometerDateTime()
        } catch (ex: DateTimeParseException) {
            throw IllegalArgumentException("Invalid measurement date token: $token", ex)
        }
    }

    protected open fun extractTemperature(rawValue: Int): Double {
        val normalizedTemperature = when (rawValue) {
            in BEFORE_MEAL_TEMPERATURE_RANGE -> rawValue
            in AFTER_MEAL_TEMPERATURE_RANGE -> rawValue - AFTER_MEAL_TEMPERATURE_SHIFT
            else -> throw IllegalArgumentException("Invalid temperature token: $rawValue")
        }
        return (normalizedTemperature.toDouble() / TEN_DIVISOR).round(1)
    }

    protected open fun extractValue(rawValue: Int): Double = rawValue.toDouble() / TEN_DIVISOR

    protected open fun extractMealTag(rawTemperature: Int): MealTag? {
        return when (rawTemperature) {
            in AFTER_MEAL_TEMPERATURE_RANGE -> MealTag.AFTERMEAL
            in BEFORE_MEAL_TEMPERATURE_RANGE -> MealTag.BEFOREMEAL
            else -> null
        }
    }
}

private data class ParsedMeasurement(
    val idToken: String,
    val date: ZonedDateTime?,
    val temperature: Double?,
    val value: Double?,
    val mealTag: MealTag?
)

private const val HEX_RADIX = 16
private const val TEN_DIVISOR = 10.0
private const val AFTER_MEAL_TEMPERATURE_SHIFT = 500
private const val MEM_AFTER_MEAL_BIT_MASK = 0x0004
private const val MEM_EMPTY_EVENT = "mem.empty"

private val BEFORE_MEAL_TEMPERATURE_RANGE = 100..350
private val AFTER_MEAL_TEMPERATURE_RANGE = 600..850

private val RD_EVENT_REGEX = Regex("^rd(\\d{12})(\\d{3})(\\d{3})$", RegexOption.IGNORE_CASE)
private val MEM_EVENT_REGEX =
    Regex("^mem\\.([0-9A-F]{8})([0-9A-F]{4})([0-9A-F]{4})$", RegexOption.IGNORE_CASE)
