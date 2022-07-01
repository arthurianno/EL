@file:Suppress("MagicNumber")

package com.elta.android.domain.features.diary.home.model

import com.elta.android.common.utils.toMillis
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

internal fun atEndOhHour(h: Int = 0): Long =
    LocalDateTime.of(LocalDate.now(), LocalTime.of(h, 59, 59)).toMillis()

internal fun atHour(h: Int = 0): Long =
    LocalDateTime.of(LocalDate.now(), LocalTime.of(h, 0, 0)).toMillis()

internal fun atHalfPastHour(h: Int = 0): Long =
    LocalDateTime.of(LocalDate.now(), LocalTime.of(h, 30, 0)).toMillis()

internal fun atStartOfDay() = LocalDateTime.now().with(LocalTime.MIDNIGHT).toMillis()
