package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.fromGlucometerDateTime
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.diary.events.model.MealTag
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
        glucometerSerialNumber: String?,
        glucometerName: String?
    ): GlucometerEvent {
        val tokens = getTokens(response)
        val dateToken = tokens.first
        val temperatureAndValueToken = tokens.second

        // Извлекаем meal tag только для Voice, для SatelliteOnline игнорируем
        val isVoiceGlucometer = glucometerName?.contains("Voice", ignoreCase = true) == true
        val mealTag = if (isVoiceGlucometer) {
            extractMealTag(response)
        } else {
            null
        }

        // Логирование для отладки
        Timber.d("📊 Glucometer response: '$response' (length=${response.length})")
        Timber.d("📊 Glucometer name: $glucometerName, isVoice: $isVoiceGlucometer")
        Timber.d("📊 Extracted mealTag: $mealTag (${if (!isVoiceGlucometer) "ignored for SatelliteOnline" else "processed for Voice"})")

        return GlucometerEvent(
            id = generator.generate(userId, glucometerId, dateToken),
            date = extractDate(dateToken),
            temperature = extractTemperature(temperatureAndValueToken),
            value = extractValue(temperatureAndValueToken),
            glucometerSerialNumber = glucometerSerialNumber,
            originalResponse = response,
            mealTag = mealTag
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

    protected open fun extractTemperature(token: String): Double? {
        val rawTemp = token.substring(0, 3).toDouble()
        // Если температура >= 600, то это "после еды" и нужно вычесть 500
        val actualTemp = if (rawTemp >= 600) rawTemp - 500 else rawTemp
        return (actualTemp / 10).round(1)
    }

    protected open fun extractValue(token: String): Double? =
        token.substring(3, 6).toDouble().div(10)

    /**
     * Извлекает meal tag (состояние до/после еды) из response строки глюкометра.
     *
     * Формат response: "rd" + дата(12) + температура(3) + значение(3) = 18 символов
     *
     * Meal tag закодирован В ЗНАЧЕНИИ ТЕМПЕРАТУРЫ:
     * - Если ttt = 100-350: измерение "до еды" (BEFOREMEAL), температура как есть
     * - Если ttt = 600-850: измерение "после еды" (AFTERMEAL), реальная температура = ttt - 500
     *
     * Пример из протокола:
     * rd220224040000245044 -> температура 245 (24.5°C), ДО ЕДЫ (BEFOREMEAL)
     * rd220224050000745044 -> температура 745-500=245 (24.5°C), ПОСЛЕ ЕДЫ (AFTERMEAL)
     */
    protected open fun extractMealTag(response: String): MealTag? {
        return try {
            val cleaned = response.replace("rd", "")
            Timber.d("📊 Cleaned response (without 'rd'): '$cleaned' (length=${cleaned.length})")

            if (cleaned.length >= 18) {
                val temperatureRaw = cleaned.substring(12, 15).toDouble()
                Timber.d("📊 Raw temperature value: $temperatureRaw")

                val result = when {
                    temperatureRaw >= 600 && temperatureRaw <= 850 -> {
                        Timber.d("📊 Temperature $temperatureRaw is in range 600-850 -> AFTERMEAL")
                        MealTag.AFTERMEAL
                    }
                    temperatureRaw >= 100 && temperatureRaw <= 350 -> {
                        Timber.d("📊 Temperature $temperatureRaw is in range 100-350 -> BEFOREMEAL")
                        MealTag.BEFOREMEAL
                    }
                    else -> {
                        Timber.w("📊 Temperature $temperatureRaw is out of valid ranges, returning null")
                        null
                    }
                }
                Timber.d("📊 MealTag result: $result")
                result
            } else {
                Timber.d("📊 Response too short (${cleaned.length} < 18), no meal tag data")
                null
            }
        } catch (ex: Exception) {
            Timber.e(ex, "❌ Error extracting meal tag from response: $response")
            null
        }
    }
}
