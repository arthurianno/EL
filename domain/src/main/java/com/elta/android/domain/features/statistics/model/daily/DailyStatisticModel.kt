package com.elta.android.domain.features.statistics.model.daily

import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import java.util.Date

data class DailyStatisticModel(
    val date: Date,
    val glucose: GlucoseStatisticModel,
    val insulin: DailyInsulinStatisticModel,
    val bread: DailyBreadStatisticModel,
    val activity: ActivityStatisticModel
)