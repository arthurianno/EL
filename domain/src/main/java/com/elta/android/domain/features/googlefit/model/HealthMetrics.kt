package com.elta.android.domain.features.googlefit.model

import java.time.Instant

/**
 * Health metrics data from Health Connect
 */
data class HealthMetrics(
    val bloodGlucose: List<BloodGlucoseData> = emptyList(),
    val bloodPressure: List<BloodPressureData> = emptyList(),
    val weight: List<WeightData> = emptyList(),
    val heartRate: List<HeartRateData> = emptyList(),
    val calories: List<CaloriesData> = emptyList()
)

data class BloodGlucoseData(
    val level: Double, // in mmol/L
    val time: Instant,
    val specimenSource: String? = null,
    val mealType: String? = null
)

data class BloodPressureData(
    val systolic: Double, // in mmHg
    val diastolic: Double, // in mmHg
    val time: Instant,
    val bodyPosition: String? = null
)

data class WeightData(
    val weightKg: Double,
    val time: Instant
)

data class HeartRateData(
    val bpm: Long,
    val time: Instant
)

data class CaloriesData(
    val kilocalories: Double,
    val startTime: Instant,
    val endTime: Instant
)

