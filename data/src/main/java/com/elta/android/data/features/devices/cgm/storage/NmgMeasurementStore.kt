package com.elta.android.data.features.devices.cgm.storage

import com.elta.android.data.features.common.cache.BoxScope
import com.elta.android.data.features.common.cache.BoxStoreFactory
import com.elta.android.data.features.devices.cgm.protocol.NmgAdvertisement
import com.elta.android.data.features.devices.cgm.protocol.NmgHistoryPoint
import io.objectbox.Box
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder.StringOrder.CASE_SENSITIVE
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NmgMeasurementStore @Inject constructor(
    private val boxStoreFactory: BoxStoreFactory
) {
    private val box: Box<NmgMeasurementEntity>
        get() = boxStoreFactory.getBoxStore(BoxScope.PER_APP)
            .boxFor(NmgMeasurementEntity::class.java)

    /**
     * The sensor has a 120-second history grid. Replacing a point within its grid bucket avoids
     * retaining 21,600 nearly identical advertising packets per day while preserving 24 hours.
     */
    fun save(advertisement: NmgAdvertisement, receivedAtEpochMillis: Long) {
        val removedFuturePoints = removeFuturePoints(
            sensorId = advertisement.sensorId,
            latestTimestampSeconds = advertisement.uptimeSeconds
        )
        val bucket = advertisement.uptimeSeconds / HISTORY_INTERVAL_SECONDS
        val storedPoint = box.query {
            equal(NmgMeasurementEntity_.sampleKey, sampleKey(advertisement.sensorId, bucket), CASE_SENSITIVE)
        }.findFirst()
        val isNewHistoryBucket = storedPoint == null
        // Immediately after a lost connection the sensor can advertise a temporary zero before
        // it resumes reporting its signal. Keep a genuine zero when it is the only reading for
        // the interval, but replace that provisional first packet once a signal arrives.
        val replacesRecoveryZero = storedPoint?.glucoseSignalNanoAmp == 0 &&
            advertisement.glucoseSignalNanoAmp != 0
        if (isNewHistoryBucket || replacesRecoveryZero) {
            savePoint(
                sensorId = advertisement.sensorId,
                state = advertisement.state,
                point = NmgHistoryPoint(
                    uptimeSeconds = advertisement.uptimeSeconds,
                    temperatureCode = advertisement.temperatureCode,
                    glucoseSignalNanoAmp = advertisement.glucoseSignalNanoAmp
                ),
                currentNanoAmp = advertisement.currentNanoAmp,
                receivedAtEpochMillis = receivedAtEpochMillis
            )
        }
        trimToHistoryLimit(advertisement.sensorId)
        Log.i(
            TAG,
            "NMG_STORE_LIVE sensorId=${advertisement.sensorId} signal=${advertisement.glucoseSignalNanoAmp} " +
                "historyAdded=$isNewHistoryBucket recoveryZeroReplaced=$replacesRecoveryZero " +
                "futureRemoved=$removedFuturePoints " +
                "count=${measurements(advertisement.sensorId).size}"
        )
    }

    fun saveHistory(
        sensorId: String,
        state: Int,
        points: List<NmgHistoryPoint>,
        latestTimestampSeconds: Long,
        receivedAtEpochMillis: Long
    ) {
        // Some sensors return a fixed five-record window even when the requested sensor time is
        // near the beginning of its lifetime. Records beyond the current advertisement timestamp
        // are future slots, not measurements, and must not block upcoming two-minute buckets.
        val validPoints = points.filter { it.uptimeSeconds <= latestTimestampSeconds }
        val ignoredFuturePoints = points.size - validPoints.size
        validPoints.forEach { point ->
            savePoint(
                sensorId = sensorId,
                state = state,
                point = point,
                currentNanoAmp = 0,
                receivedAtEpochMillis = receivedAtEpochMillis -
                    (latestTimestampSeconds - point.uptimeSeconds).coerceAtLeast(0) * 1_000L
            )
        }
        trimToHistoryLimit(sensorId)
        Log.i(
            TAG,
            "NMG_STORE_HISTORY sensorId=$sensorId points=${validPoints.size} " +
                "futureIgnored=$ignoredFuturePoints total=${measurements(sensorId).size}"
        )
    }

    fun measurements(sensorId: String): List<NmgMeasurementEntity> = box.query {
        equal(NmgMeasurementEntity_.sensorId, sensorId, CASE_SENSITIVE)
    }.find().sortedBy { it.uptimeSeconds }

    fun latestUptimeSeconds(sensorId: String): Long? = measurements(sensorId).lastOrNull()?.uptimeSeconds

    private fun trimToHistoryLimit(sensorId: String) {
        val obsolete = measurements(sensorId).dropLast(MAX_HISTORY_POINTS)
        if (obsolete.isNotEmpty()) box.remove(obsolete)
    }

    private fun removeFuturePoints(sensorId: String, latestTimestampSeconds: Long): Int {
        val futurePoints = measurements(sensorId)
            .filter { it.uptimeSeconds > latestTimestampSeconds }
        if (futurePoints.isNotEmpty()) box.remove(futurePoints)
        return futurePoints.size
    }

    private fun savePoint(
        sensorId: String,
        state: Int,
        point: NmgHistoryPoint,
        currentNanoAmp: Int,
        receivedAtEpochMillis: Long
    ) {
        val bucket = point.uptimeSeconds / HISTORY_INTERVAL_SECONDS
        val key = sampleKey(sensorId, bucket)
        val existing = box.query {
            equal(NmgMeasurementEntity_.sampleKey, key, CASE_SENSITIVE)
        }.findFirst()

        box.put(
            (existing ?: NmgMeasurementEntity(sampleKey = key)).apply {
                this.sensorId = sensorId
                uptimeBucket = bucket
                uptimeSeconds = point.uptimeSeconds
                this.receivedAtEpochMillis = receivedAtEpochMillis
                this.state = state
                temperatureCode = point.temperatureCode
                glucoseSignalNanoAmp = point.glucoseSignalNanoAmp
                this.currentNanoAmp = currentNanoAmp
            }
        )
    }

    private fun sampleKey(sensorId: String, bucket: Long): String = "$sensorId:$bucket"

    private companion object {
        const val TAG = "NmgMonitoring"
        const val HISTORY_INTERVAL_SECONDS = 120L
        const val MAX_HISTORY_POINTS = 720
    }
}
