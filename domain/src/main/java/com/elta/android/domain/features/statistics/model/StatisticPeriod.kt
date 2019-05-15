package com.elta.android.domain.features.statistics.model

import java.util.Date

interface StatisticPeriod {
    val start: Date
    val end: Date
}