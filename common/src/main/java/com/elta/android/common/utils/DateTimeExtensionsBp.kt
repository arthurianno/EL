package com.elta.android.common.utils

import android.util.LruCache
import com.elta.android.common.utils.CommonFormats.DATE_PATTERN_ISO
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.TemporalAccessor
import java.util.Locale

object DateTimeFormatterCache {
    private const val CACHE_SIZE = 16
    private val cache = LruCache<String, DateTimeFormatter?>(CACHE_SIZE)

    operator fun get(name: String): DateTimeFormatter? = cache[name]

    operator fun set(name: String, formatter: DateTimeFormatter) {
        cache.put(name, formatter)
    }

    fun getOrCreateFormatter(
        pattern: String,
        locale: Locale = Locale.getDefault()
    ): DateTimeFormatter {
        val key = "$pattern${locale.displayName ?: ""}"
        var format = this[key]
        if (format == null) {
            format = DateTimeFormatter.ofPattern(pattern, locale)
            this[key] = format
        }
        return checkNotNull(format)
    }
}

fun TemporalAccessor.toStringWithFormat(
    pattern: String,
    locale: Locale = Locale.getDefault()
): String {
    return DateTimeFormatterCache.getOrCreateFormatter(pattern, locale)
        .format(this)
}

fun TemporalAccessor.toStringWithFormat(format: DateTimeFormatter): String =
    format.format(this)

fun String.toDate(pattern: String): ZonedDateTime =
    ZonedDateTime.parse(this, DateTimeFormatterCache.getOrCreateFormatter(pattern))

fun String.toDate(formatter: DateTimeFormatter): ZonedDateTime =
    ZonedDateTime.parse(this, formatter)

fun String.toLocalDateTime(pattern: String): LocalDateTime =
    LocalDateTime.parse(this, DateTimeFormatterCache.getOrCreateFormatter(pattern))

fun String.toIsoDate(): ZonedDateTime = ZonedDateTime.parse(
    this,
    DateTimeFormatterCache.getOrCreateFormatter(DATE_PATTERN_ISO)
)

fun ZonedDateTime.toIsoString(): String = this.format(
    DateTimeFormatterCache.getOrCreateFormatter(DATE_PATTERN_ISO)
)

fun LocalDateTime.atStartOfDay(): LocalDateTime = this.with(LocalTime.MIDNIGHT)
fun LocalDateTime.atEndOfDay(): LocalDateTime = this.with(LocalTime.MAX)

fun ZonedDateTime.atStartOfDay(): ZonedDateTime = this.with(LocalTime.MIDNIGHT)
fun ZonedDateTime.atEndOfDay(): ZonedDateTime = this.with(LocalTime.MAX)

fun LocalDate.isToday(): Boolean = LocalDate.now() == this
fun LocalDate.isYesterday(): Boolean = LocalDate.now().minusDays(1) == this

fun systemOffset(): ZoneOffset = ZoneOffset.systemDefault().rules.getOffset(LocalDateTime.now())

fun LocalDateTime.toMillis(offset: ZoneOffset = systemOffset()) = toInstant(offset).toEpochMilli()

fun LocalDate.toMillis(offset: ZoneOffset = systemOffset()) =
    atStartOfDay().toInstant(offset).toEpochMilli()

fun ZonedDateTime.toMillis() = toInstant().toEpochMilli()

fun ZonedDateTime.toMillisUtc() =
    toInstant().atOffset(ZoneOffset.UTC).toEpochSecond() * MILLIS_IN_SECOND

fun Long.toZonedDateTime() =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun ZonedDateTime.toDateTimeUtc() = toOffsetDateTime().atZoneSameInstant(ZoneOffset.UTC)

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

    const val DATE_PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"
}
