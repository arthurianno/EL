package com.elta.android.domain.features.statistics.model.daily

import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import org.threeten.bp.LocalDate

data class DailyStatisticModel(
    val date: LocalDate,
    val glucose: GlucoseStatisticModel,
    val insulin: DailyInsulinStatisticModel,
    val bread: DailyBreadStatisticModel,
    val activity: ActivityStatisticModel
)