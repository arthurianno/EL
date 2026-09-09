package com.elta.android.data.features.devices.cgm.repository

import com.elta.android.data.features.devices.cgm.client.NmgScanner
import com.elta.android.data.features.devices.cgm.service.NmgMonitoringController
import com.elta.android.data.features.devices.cgm.storage.NmgMeasurementStore
import com.elta.android.data.features.devices.cgm.storage.NmgMonitoringPreferences
import com.elta.android.domain.features.cgm.model.NmgMeasurement
import com.elta.android.domain.features.cgm.model.NmgMonitoringState
import com.elta.android.domain.features.cgm.model.NmgSensor
import com.elta.android.domain.features.cgm.repository.NmgRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NmgDataRepository @Inject constructor(
    private val scanner: NmgScanner,
    private val monitoringController: NmgMonitoringController,
    private val preferences: NmgMonitoringPreferences,
    private val measurementStore: NmgMeasurementStore
) : NmgRepository {
    override fun findSensors(): Flow<List<NmgSensor>> = scanner.findSensors()
        .map { sensors ->
            sensors
                .filterNot { it.equals(preferences.activeSensorId, ignoreCase = true) }
                .map(::NmgSensor)
        }

    override fun startMonitoring(sensorId: String) = monitoringController.start(sensorId)

    override fun resumeMonitoring() = monitoringController.resumeIfActive()

    override fun stopMonitoring() = monitoringController.stop()

    override fun activeSensorId(): String? = preferences.activeSensorId

    override fun isPrimary(): Boolean = preferences.isPrimary

    override fun measurements(): List<NmgMeasurement> = preferences.activeSensorId
        ?.let(measurementStore::measurements)
        .orEmpty()
        .map { measurement ->
            NmgMeasurement(
                sensorId = measurement.sensorId,
                uptimeSeconds = measurement.uptimeSeconds,
                receivedAtEpochMillis = measurement.receivedAtEpochMillis,
                state = measurement.state,
                glucoseSignalNanoAmp = measurement.glucoseSignalNanoAmp,
                currentNanoAmp = measurement.currentNanoAmp
            )
        }

    override fun latestLiveMeasurement(): NmgMeasurement? = preferences.activeSensorId
        ?.let(preferences::liveMeasurement)

    override fun monitoringState(): Flow<NmgMonitoringState> = flow {
        while (true) {
            val measurements = measurements()
            emit(
                NmgMonitoringState(
                    sensorId = preferences.activeSensorId,
                    lastMeasurement = measurements.lastOrNull(),
                    historySize = measurements.size
                )
            )
            delay(MONITORING_STATE_INTERVAL_MILLIS)
        }
    }

    private companion object {
        const val MONITORING_STATE_INTERVAL_MILLIS = 4_000L
    }
}
