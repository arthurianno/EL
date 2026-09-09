package com.elta.android.data.features.devices.cgm.storage

import android.content.SharedPreferences
import com.elta.android.data.features.devices.cgm.protocol.NmgAdvertisement
import com.elta.android.domain.features.cgm.model.NmgMeasurement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NmgMonitoringPreferences @Inject constructor(
    private val preferences: SharedPreferences
) {
    var activeSensorId: String?
        get() = preferences.getString(ACTIVE_SENSOR_ID, null)
        set(value) = preferences.edit().putString(ACTIVE_SENSOR_ID, value).apply()

    var isPrimary: Boolean
        get() = preferences.getBoolean(IS_PRIMARY, false)
        set(value) = preferences.edit().putBoolean(IS_PRIMARY, value).apply()

    var lastAdvertisementAtEpochMillis: Long
        get() = preferences.getLong(LAST_ADVERTISEMENT_AT, 0)
        set(value) = preferences.edit().putLong(LAST_ADVERTISEMENT_AT, value).apply()

    var lossNotificationSent: Boolean
        get() = preferences.getBoolean(LOSS_NOTIFICATION_SENT, false)
        set(value) = preferences.edit().putBoolean(LOSS_NOTIFICATION_SENT, value).apply()

    fun saveLiveMeasurement(advertisement: NmgAdvertisement, receivedAtEpochMillis: Long) {
        preferences.edit()
            .putString(LIVE_SENSOR_ID, advertisement.sensorId)
            .putLong(LIVE_UPTIME_SECONDS, advertisement.uptimeSeconds)
            .putLong(LIVE_RECEIVED_AT, receivedAtEpochMillis)
            .putInt(LIVE_STATE, advertisement.state)
            .putInt(LIVE_SIGNAL, advertisement.glucoseSignalNanoAmp)
            .putInt(LIVE_CURRENT, advertisement.currentNanoAmp)
            .apply()
    }

    fun liveMeasurement(sensorId: String): NmgMeasurement? {
        if (!sensorId.equals(preferences.getString(LIVE_SENSOR_ID, null), ignoreCase = true)) return null
        val receivedAt = preferences.getLong(LIVE_RECEIVED_AT, 0L)
        if (receivedAt == 0L) return null
        return NmgMeasurement(
            sensorId = sensorId,
            uptimeSeconds = preferences.getLong(LIVE_UPTIME_SECONDS, 0L),
            receivedAtEpochMillis = receivedAt,
            state = preferences.getInt(LIVE_STATE, 0),
            glucoseSignalNanoAmp = preferences.getInt(LIVE_SIGNAL, 0),
            currentNanoAmp = preferences.getInt(LIVE_CURRENT, 0)
        )
    }

    fun clear() {
        preferences.edit()
            .remove(ACTIVE_SENSOR_ID)
            .remove(IS_PRIMARY)
            .remove(LAST_ADVERTISEMENT_AT)
            .remove(LOSS_NOTIFICATION_SENT)
            .remove(LIVE_SENSOR_ID)
            .remove(LIVE_UPTIME_SECONDS)
            .remove(LIVE_RECEIVED_AT)
            .remove(LIVE_STATE)
            .remove(LIVE_SIGNAL)
            .remove(LIVE_CURRENT)
            .apply()
    }

    private companion object {
        const val ACTIVE_SENSOR_ID = "nmg_active_sensor_id"
        const val IS_PRIMARY = "nmg_is_primary"
        const val LAST_ADVERTISEMENT_AT = "nmg_last_advertisement_at"
        const val LOSS_NOTIFICATION_SENT = "nmg_loss_notification_sent"
        const val LIVE_SENSOR_ID = "nmg_live_sensor_id"
        const val LIVE_UPTIME_SECONDS = "nmg_live_uptime_seconds"
        const val LIVE_RECEIVED_AT = "nmg_live_received_at"
        const val LIVE_STATE = "nmg_live_state"
        const val LIVE_SIGNAL = "nmg_live_signal"
        const val LIVE_CURRENT = "nmg_live_current"
    }
}
