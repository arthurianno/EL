package com.elta.android.domain.features.statistics.model

import java.util.Date

data class PeriodStatisticModel(
    val period: StatisticPeriod,
    val dayWithMaxLevel: DayStatisticModel,
    val dayWithMinLevel: DayStatisticModel,
    val allDays: Map<Date, DayStatisticModel>,

    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModel,
    val bread: BreadStatisticModel,
    val activity: ActivityStatisticModel
)