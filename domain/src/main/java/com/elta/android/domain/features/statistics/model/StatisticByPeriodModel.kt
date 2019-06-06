package com.elta.android.domain.features.statistics.model

import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import org.threeten.bp.LocalDate

data class StatisticByPeriodModel(
    val period: StatisticPeriod,
    val dayWithMaxLevel: DailyStatisticModel?,
    val dayWithMinLevel: DailyStatisticModel?,
    val allDays: Map<LocalDate, DailyStatisticModel>,

    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModelByPeriod,
    val bread: BreadStatisticModelByPeriod,
    val activity: ActivityStatisticModel
)