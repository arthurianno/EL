package com.elta.android.domain.features.statistics.model.daily

import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.BreadStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.InsulinStatisticModelByPeriod
import java.util.Date

data class DailyStatisticModel(
    val date: Date,
    val glucose: GlucoseStatisticModel,
    val insulin: InsulinStatisticModelByPeriod,
    val bread: BreadStatisticModelByPeriod,
    val activity: ActivityStatisticModel
)