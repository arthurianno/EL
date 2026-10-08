package com.elta.android.presentation.features.main.records.ui.compose

import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.model.glucoseManagementIndicatorPercent
import kotlin.math.roundToLong
import kotlin.math.sqrt

internal data class ContinuousStatistics(
    val points: List<DetailedGlucosePoint>, val count: Int, val average: Float?, val normal: Int, val high: Int, val low: Int,
    val sd: Float?, val cv: Int?, val gmi: Float?
) {
    companion object {
        fun from(points: List<DetailedGlucosePoint>, settings: GlucoseLevelSettings?): ContinuousStatistics {
            val average = points.takeIf { it.isNotEmpty() }?.map { it.value }?.average()?.toFloat()
            val normal = points.count { settings?.normal?.contains(it.value.toDouble()) ?: (it.value in 3.9f..10f) }
            val high = points.count { settings?.high?.contains(it.value.toDouble()) ?: (it.value > 10f) }
            val low = points.count { settings?.low?.contains(it.value.toDouble()) ?: (it.value < 3.9f) }
            val sd = if (points.size >= 2) average?.let { mean -> sqrt(points.map { (it.value - mean) * (it.value - mean) }.average()).toFloat() } else null
            val cv = if (average != null && average > 0f && sd != null) (sd / average * 100).roundToLong().toInt() else null
            val gmi = average?.let { glucoseManagementIndicatorPercent(it.toDouble()).toFloat() }
            return ContinuousStatistics(points, points.size, average, normal, high, low, sd, cv, gmi)
        }
    }
}
