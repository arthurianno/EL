package com.elta.android.domain.features.statistics.model

import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings

data class GlucoseStatisticModel(
    val settings: GlucoseLevelSettings,

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

    val eventsHighPercent: Int,
    val eventsNormalPercent: Int,
    val eventsLowPercent: Int,

    val dailyGlucoseModel: DailyGlucoseModel?
)