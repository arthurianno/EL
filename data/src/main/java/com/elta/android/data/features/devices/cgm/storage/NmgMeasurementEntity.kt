package com.elta.android.data.features.devices.cgm.storage

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

@Entity
data class NmgMeasurementEntity(
    @Id var id: Long = 0,
    @Unique var sampleKey: String = "",
    @Index var sensorId: String = "",
    var uptimeBucket: Long = 0,
    var uptimeSeconds: Long = 0,
    var receivedAtEpochMillis: Long = 0,
    var state: Int = 0,
    var temperatureCode: Int = 0,
    var glucoseSignalNanoAmp: Int = 0,
    var currentNanoAmp: Int = 0
)
