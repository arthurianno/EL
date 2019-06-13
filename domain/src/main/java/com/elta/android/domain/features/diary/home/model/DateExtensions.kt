@file:Suppress("MagicNumber")

package com.elta.android.domain.features.diary.home.model

import com.elta.android.common.utils.toMillis
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

inline fun atEndOhHour(h: Int = 0): Long =
    LocalDateTime.of(LocalDate.now(), LocalTime.of(h, 59, 59)).toMillis()

inline fun atHour(h: Int = 0): Long =
    LocalDateTime.of(LocalDate.now(), LocalTime.of(h, 0, 0)).toMillis()

inline fun atHalfPastHour(h: Int = 0): Long =
    LocalDateTime.of(LocalDate.now(), LocalTime.of(h, 30, 0)).toMillis()

inline fun atStartOfDay() = LocalDateTime.now().with(LocalTime.MIDNIGHT).toMillis()