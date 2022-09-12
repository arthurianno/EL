package com.elta.android.domain.features.statistics.model

import org.threeten.bp.LocalDateTime

interface StatisticPeriod {
    val start: LocalDateTime
    val end: LocalDateTime
}
