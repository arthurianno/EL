package com.elta.android.domain.features.statistics.model

import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import java.util.Date

data class StatisticByPeriodModel(
    val period: StatisticPeriod,
    val dayWithMaxLevel: DailyStatisticModel?,
    val dayWithMinLevel: DailyStatisticModel?,
    val allDays: Map<Date, DailyStatisticModel>,

    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModelByPeriod,
    val bread: BreadStatisticModelByPeriod,
    val activity: ActivityStatisticModel
)