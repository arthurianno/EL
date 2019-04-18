package com.elta.android.domain.features.statistics.model

import java.util.Date

data class StatisticByDateModel(
    val date: Date,
    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModel,
    val bread: BreadStatisticModel,
    val activity: ActivityStatisticModel
)