package com.elta.android.domain.features.statistics.model

import java.util.Date

data class DailyStatisticModel(
    val date: Date,
    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModelByPeriod,
    val bread: BreadStatisticModelByPeriod,
    val activity: ActivityStatisticModel
)