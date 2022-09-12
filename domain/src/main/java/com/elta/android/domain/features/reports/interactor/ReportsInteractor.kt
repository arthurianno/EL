package com.elta.android.domain.features.reports.interactor

import com.elta.android.domain.features.reports.model.Range
import org.threeten.bp.LocalDate

private const val RANGE_DAYS_OFFSET = 13L

fun buildRange(now: LocalDate = LocalDate.now()): Range =
    Range(start = now.minusDays(RANGE_DAYS_OFFSET), end = now)
