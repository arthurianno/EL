package com.elta.android.domain.features.statistics.model

data class GlucoseStatisticModel(
    val averageLevel: Double,
    val maxLevel: Double,
    val minLevel: Double,

    val maxHighLevel: Double?,
    val minHighLevel: Double?,
    val maxNormalLevel: Double?,
    val minNormalLevel: Double?,
    val maxLowLevel: Double?,
    val minLowLevel: Double?,

    val eventsCount: Int,
    val eventsHighCount: Int,
    val eventsNormalCount: Int,
    val eventsLowCount: Int,

    val eventsHighPercent: Double,
    val eventsNormalPercent: Double,
    val eventsLowPercent: Double
)