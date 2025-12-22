

package com.elta.android.domain.features.statistics.model.daily

import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import org.threeten.bp.LocalDateTime

data class HourlyStatisticModel(
    val dateTimeStart: LocalDateTime,
    val glucose: GlucoseStatisticModel?,
    val insulin: DailyInsulinStatisticModel?,  // Using DailyInsulinStatisticModel for totals, similar to day
    val bread: DailyBreadStatisticModel?,
    val activity: ActivityStatisticModel?  // Using ActivityStatisticModel, with eventsCount and averageDuration (can compute total if needed)
)