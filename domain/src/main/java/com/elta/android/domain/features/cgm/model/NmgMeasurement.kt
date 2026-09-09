package com.elta.android.domain.features.cgm.model

/**
 * The current firmware exposes an electrical test signal, not calibrated glucose.
 * `glucoseSignalNanoAmp` must never be rendered as mmol/L before a calibration contract exists.
 */
data class NmgMeasurement(
    val sensorId: String,
    val uptimeSeconds: Long,
    val receivedAtEpochMillis: Long,
    val state: Int,
    val glucoseSignalNanoAmp: Int,
    val currentNanoAmp: Int
)
