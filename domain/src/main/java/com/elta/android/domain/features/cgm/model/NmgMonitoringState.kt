package com.elta.android.domain.features.cgm.model

data class NmgMonitoringState(
    val sensorId: String?,
    val lastMeasurement: NmgMeasurement?,
    val historySize: Int
)
