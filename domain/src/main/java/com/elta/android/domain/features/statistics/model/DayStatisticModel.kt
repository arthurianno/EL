package com.elta.android.domain.features.statistics.model

import java.util.Date

data class DayStatisticModel(
    val date: Date,
    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModel,
    val bread: BreadStatisticModel,
    val activity: ActivityStatisticModel
)