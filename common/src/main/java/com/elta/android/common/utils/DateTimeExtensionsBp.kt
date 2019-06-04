package com.elta.android.common.utils

import android.util.LruCache
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAccessor
import java.util.Locale

object DateTimeFormatterCache {
    private val cache = LruCache<String, DateTimeFormatter?>(16)

    operator fun get(name: String): DateTimeFormatter? {
        return cache[name]
    }

    operator fun set(name: String, formatter: DateTimeFormatter) {
        cache.put(name, formatter)
    }

    fun getOrCreateFormatter(pattern: String, locale: Locale = Locale.getDefault()): DateTimeFormatter {
        val key = "$pattern${locale.displayName ?: ""}"
        var format = this[key]
        if (format == null) {
            format = DateTimeFormatter.ofPattern(pattern, locale)
            this[key] = format
        }
        return checkNotNull(format)
    }
}

inline fun TemporalAccessor.toStringWithFormat(pattern: String, locale: Locale = Locale.getDefault()): String {
    return DateTimeFormatterCache.getOrCreateFormatter(pattern, locale)
        .format(this)
}

inline fun TemporalAccessor.toStringWithFormat(format: DateTimeFormatter): String =
    format.format(this)

inline fun String.toDate(pattern: String): ZonedDateTime =
    ZonedDateTime.parse(this, DateTimeFormatterCache.getOrCreateFormatter(pattern))

inline fun String.toDate(formatter: DateTimeFormatter): ZonedDateTime =
    ZonedDateTime.parse(this, formatter)

inline fun String.toIsoDate(): ZonedDateTime = ZonedDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
inline fun ZonedDateTime.toIsoString(): String = this.format(DateTimeFormatter.ISO_DATE_TIME)

inline fun LocalDateTime.atStartOfDay() = this.with(LocalTime.MIDNIGHT)
inline fun LocalDateTime.atEndOfDay() = this.with(LocalTime.MAX)

inline fun ZonedDateTime.atStartOfDay() = this.with(LocalTime.MIDNIGHT)
inline fun ZonedDateTime.atEndOfDay() = this.with(LocalTime.MAX)

inline fun LocalDate.isToday(): Boolean {
    val today = ZonedDateTime.now().toLocalDate()
    val tomorrow = today.plusDays(1)
    return today.rangeTo(tomorrow).contains(this)
}

inline fun LocalDate.isYesterday(): Boolean {
    val today = ZonedDateTime.now().toLocalDate()
    val tomorrow = today.minusDays(1)
    return tomorrow.rangeTo(today).contains(this)
}

inline fun systemOffset(): ZoneOffset = ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now())

inline fun LocalDateTime.toMillis(offset: ZoneOffset = systemOffset()) = toInstant(offset).toEpochMilli()

inline fun LocalDate.toMillis(offset: ZoneOffset = systemOffset()) = atStartOfDay().toInstant(offset).toEpochMilli()

inline fun ZonedDateTime.toMillis() = toInstant().toEpochMilli()

/**
 * Simple class which contains number of common and wide useful date formats.
 * @author Grishko Nikita
 */
object CommonFormats {
    /**
     * Date/time format: dd.MM.yyyy HH:mm:ss
     */
    const val FORMAT_SIMPLE_DATE_TIME_SECONDS = "dd.MM.yyyy HH:mm:ss"
    /**
     * Date/time format: dd.MM.yyyy HH:mm
     */
    const val FORMAT_SIMPLE_DATE_TIME = "dd.MM.yyyy HH:mm"
    /**
     * Date/time format: dd.MM.yyyy
     */
    const val FORMAT_SIMPLE_DATE = "dd.MM.yyyy"
    /**
     * Date/time format: dd LLL yyyy
     */
    const val FORMAT_DATE_WITH_MONTH_NAME = "dd LLL yyyy"
    /**
     * Date/time format: yyyy-MM-dd HH:mm:ss
     */
    const val FORMAT_STANDARD_DATE_TIME_SECONDS = "yyyy-MM-dd HH:mm:ss"
    /**
     * Date/time format: yyyy-MM-dd HH:mm
     */
    const val FORMAT_STANDARD_DATE_TIME = "yyyy-MM-dd HH:mm"
    /**
     * Date/time format: yyyy-MM-dd
     */
    const val FORMAT_STANDARD_DATE = "yyyy-MM-dd"
    /**
     * Date/time format: yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    const val FORMAT_STANDARD_DATE_FULL_UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    /**
     * Date/time format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    const val FORMAT_STANDARD_DATE_FULL_MILLIS_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    /**
     * Date/time format: HH:mm
     */
    const val FORMAT_TIME = "HH:mm"
    /**
     * Date/time format: HH:mm:ss
     */
    const val FORMAT_TIME_2 = "HH:mm:ss"
}