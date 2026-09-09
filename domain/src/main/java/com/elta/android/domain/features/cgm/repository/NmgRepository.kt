package com.elta.android.domain.features.cgm.repository

import com.elta.android.domain.features.cgm.model.NmgMeasurement
import com.elta.android.domain.features.cgm.model.NmgMonitoringState
import com.elta.android.domain.features.cgm.model.NmgSensor
import kotlinx.coroutines.flow.Flow

interface NmgRepository {
    fun findSensors(): Flow<List<NmgSensor>>
    fun startMonitoring(sensorId: String)
    fun resumeMonitoring()
    fun stopMonitoring()
    fun activeSensorId(): String?
    fun isPrimary(): Boolean
    fun measurements(): List<NmgMeasurement>
    fun latestLiveMeasurement(): NmgMeasurement?
    fun monitoringState(): Flow<NmgMonitoringState>
}
