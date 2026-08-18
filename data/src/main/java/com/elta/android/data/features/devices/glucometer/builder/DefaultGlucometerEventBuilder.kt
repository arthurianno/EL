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
        glucometerName: String?
    ): GlucometerEvent {
        val parsedMeasurement = parseMeasurement(
            response = response,
            supportsMealTags = glucometerName.supportsMealTags()
        )

        return GlucometerEvent(
            id = generator.generate(userId, glucometerId, parsedMeasurement.idToken),
            date = parsedMeasurement.date,
            temperature = parsedMeasurement.temperature,
            value = parsedMeasurement.value,
            glucometerSerialNumber = glucometerSerialNumber,
            originalResponse = response,
            mealTag = parsedMeasurement.mealTag,
            isTimeInvalid = parsedMeasurement.isTimeInvalid,
            isTemperatureInvalid = parsedMeasurement.isTemperatureInvalid
        )
    }

    override fun getDate(response: String): Date {
        val mills = parseMeasurement(
            response = response,
            supportsMealTags = false
        ).date?.toInstant()?.toEpochMilli() ?: 0L
        return Date(mills)
    }

    override fun getValue(response: String): Double {
        return parseMeasurement(response, supportsMealTags = false).value ?: 0.0
    }

    private fun parseMeasurement(response: String, supportsMealTags: Boolean): ParsedMeasurement {
        val normalizedResponse = response.trim()
        return when {
            RD_EVENT_REGEX.matches(normalizedResponse) ->
                parseRdMeasurement(normalizedResponse, supportsMealTags)
            MEM_EVENT_REGEX.matches(normalizedResponse) ->
                parseMemMeasurement(normalizedResponse, supportsMealTags)
            normalizedResponse.equals(MEM_EMPTY_EVENT, ignoreCase = true) ->
                throw IllegalArgumentException("Measurement payload is empty: $response")
            else -> throw IllegalArgumentException("Unsupported measurement format: $response")
        }
    }

    private fun parseRdMeasurement(response: String, supportsMealTags: Boolean): ParsedMeasurement {
        val match = checkNotNull(RD_EVENT_REGEX.matchEntire(response)) {
            "Invalid rd payload: $response"
        }

        val dateToken = match.groupValues[1]
        val rawTemperature = match.groupValues[2].toInt()
        val rawValue = match.groupValues[3].toInt()
        val date = extractDate(dateToken)
        val isInvalid = isDateInvalid(date)
        val actualDate = if (isInvalid) ZonedDateTime.now(ZoneOffset.UTC) else date

        return ParsedMeasurement(
            idToken = dateToken,
            date = actualDate,
            temperature = extractTemperature(rawTemperature),
            value = extractValue(rawValue),
            mealTag = extractMealTag(rawTemperature).takeIf { supportsMealTags },
            isTimeInvalid = isInvalid
        )
    }

    private fun parseMemMeasurement(response: String, supportsMealTags: Boolean): ParsedMeasurement {
        val match = checkNotNull(MEM_EVENT_REGEX.matchEntire(response)) {
            "Invalid mem payload: $response"
        }
        val unixHex = match.groupValues[1]
        val statusHex = match.groupValues[2]
        val glucoseHex = match.groupValues[3]

        val unixSeconds = unixHex.toLong(HEX_RADIX)
        val statusWord = statusHex.toInt(HEX_RADIX)
        val glucoseValue = glucoseHex.toInt(HEX_RADIX).toDouble() / TEN_DIVISOR
        val date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(unixSeconds), ZoneOffset.UTC)
        val isInvalid = isDateInvalid(date, statusWord)
        val isTempInvalid = (statusWord and MEM_INVALID_TEMPERATURE_BIT_MASK) != 0
        
        val actualDate = if (isInvalid) ZonedDateTime.now(ZoneOffset.UTC) else date

        return ParsedMeasurement(
            idToken = "$unixHex$glucoseHex",
            date = actualDate,
            temperature = null,
            value = glucoseValue,
            mealTag = if (supportsMealTags) {
                if (statusWord and MEM_AFTER_MEAL_BIT_MASK != 0) MealTag.AFTERMEAL else MealTag.BEFOREMEAL
            } else {
                null
            },
            isTimeInvalid = isInvalid,
            isTemperatureInvalid = isTempInvalid
        )
    }

    protected open fun isDateInvalid(date: ZonedDateTime?, statusWord: Int = 0): Boolean {
        if (date == null) return true
        if (date.year < MIN_VALID_YEAR) return true
        if ((statusWord and MEM_INVALID_TIME_BIT_MASK) != 0) return true
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        if (date.isAfter(now.plusMinutes(FUTURE_TIME_TOLERANCE_MINUTES))) return true
        return false
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
    val mealTag: MealTag?,
    val isTimeInvalid: Boolean = false,
    val isTemperatureInvalid: Boolean = false
)

private const val HEX_RADIX = 16
private const val TEN_DIVISOR = 10.0
private const val AFTER_MEAL_TEMPERATURE_SHIFT = 500
private const val MEM_AFTER_MEAL_BIT_MASK = 0x0004
private const val MEM_INVALID_TEMPERATURE_BIT_MASK = 0x0002
private const val MEM_INVALID_TIME_BIT_MASK = 0x0001
private const val MIN_VALID_YEAR = 2020
private const val FUTURE_TIME_TOLERANCE_MINUTES = 1L
private const val MEM_EMPTY_EVENT = "mem.empty"

private val BEFORE_MEAL_TEMPERATURE_RANGE = 100..350
private val AFTER_MEAL_TEMPERATURE_RANGE = 600..850

private val RD_EVENT_REGEX = Regex("^rd(\\d{12})(\\d{3})(\\d{3})$", RegexOption.IGNORE_CASE)
private val MEM_EVENT_REGEX =
    Regex("^mem\\.([0-9A-F]{8})([0-9A-F]{4})([0-9A-F]{4})$", RegexOption.IGNORE_CASE)

private fun String?.supportsMealTags(): Boolean =
    this?.startsWith(SATELLITE_VOICE_PREFIX, ignoreCase = true) == true

private const val SATELLITE_VOICE_PREFIX = "SatelliteVoice"
