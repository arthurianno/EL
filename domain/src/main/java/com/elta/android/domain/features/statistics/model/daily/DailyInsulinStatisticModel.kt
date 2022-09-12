package com.elta.android.domain.features.statistics.model.daily

data class DailyInsulinStatisticModel(
    val totalBolusLevel: Double,
    val totalBasalLevel: Double,
    val totalLevel: Double
)
