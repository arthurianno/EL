package com.elta.android.presentation.features.statistic.period.ui.compose

private const val DAYS_PER_WEEK = 7

internal val List<HourlyRange>.lastWeekOffset: Int
    get() = (size - 1).coerceAtLeast(0) / DAYS_PER_WEEK

/** Returns one calendar window, counted backwards from the latest seven days. */
internal fun List<HourlyRange>.weekFromLatest(offset: Int): List<HourlyRange> =
    dropLast(offset.coerceIn(0, lastWeekOffset) * DAYS_PER_WEEK).takeLast(DAYS_PER_WEEK)
