package com.elta.android.data.features.devices.cgm.service

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.elta.android.data.features.devices.cgm.storage.NmgMonitoringPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NmgMonitoringController @Inject constructor(
    private val context: Context,
    private val preferences: NmgMonitoringPreferences
) {
    /** Starts monitoring only after the user deliberately selected a sensor. */
    fun start(sensorId: String) {
        preferences.activeSensorId = sensorId
        preferences.isPrimary = true
        Log.i(TAG, "NMG_BIND sensorId=$sensorId")
        preferences.lastAdvertisementAtEpochMillis = 0
        preferences.lossNotificationSent = false
        ContextCompat.startForegroundService(context, CgmMonitoringService.newStartIntent(context))
    }

    fun stop() {
        Log.i(TAG, "NMG_UNBIND sensorId=${preferences.activeSensorId}")
        preferences.clear()
        context.stopService(CgmMonitoringService.newStopIntent(context))
    }

    /** Restores monitoring after a device reboot or an app update without asking the user again. */
    fun resumeIfActive() {
        val sensorId = preferences.activeSensorId ?: return
        Log.i(TAG, "NMG_RESUME sensorId=$sensorId")
        runCatching {
            ContextCompat.startForegroundService(context, CgmMonitoringService.newStartIntent(context))
        }.onFailure { error ->
            Log.w(TAG, "NMG_RESUME failed", error)
        }
    }

    private companion object {
        const val TAG = "NmgMonitoring"
    }
}
