package com.elta.android.domain.features.statistics.model

import java.util.Date

data class StatisticByPeriodModel(
    val period: StatisticPeriod,
    val dayWithMaxLevel: StatisticByDateModel?,
    val dayWithMinLevel: StatisticByDateModel?,
    val allDays: Map<Date, StatisticByDateModel>,

    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModel,
    val bread: BreadStatisticModel,
    val activity: ActivityStatisticModel
)