package com.elta.android.domain.features.statistics.model

data class InsulinStatisticModelByPeriod(
    val averageBolusLevel: Double,
    val averageBasalLevel: Double,
    val averageLevel: Double
)