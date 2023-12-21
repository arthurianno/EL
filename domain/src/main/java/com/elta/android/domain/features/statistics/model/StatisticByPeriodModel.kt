package com.elta.android.domain.features.statistics.model

import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import org.threeten.bp.LocalDate

data class StatisticByPeriodModel(
    val period: StatisticPeriod,
    val dayWithMaxLevel: DailyStatisticModel?,
    val dayWithMinLevel: DailyStatisticModel?,
    val allDays: Map<LocalDate, DailyStatisticModel>,
    val calculatorFlow: CalculatorFlow,

    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModelByPeriod,
    val food: BreadStatisticModelByPeriod,
    val activity: ActivityStatisticModel
)
