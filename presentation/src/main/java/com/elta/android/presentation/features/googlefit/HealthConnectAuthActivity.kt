package com.elta.android.presentation.features.googlefit

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord

import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.SingletonRxBusProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Activity for requesting Health Connect permissions
 * Only used on Android 14+ (API 34+)
 */
class HealthConnectAuthActivity : ComponentActivity() {

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        sendResult(allGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Timber.w("Health Connect not available on Android < 14")
            sendResult(false)
            return
        }

        val healthConnectClient = HealthConnectClient.getOrCreate(this)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requiredPermissions = setOf(
                    // Activity & Steps
                    HealthPermission.getReadPermission(ExerciseSessionRecord::class),
                    HealthPermission.getReadPermission(StepsRecord::class),
                    // Health metrics
                    HealthPermission.getReadPermission(BloodGlucoseRecord::class),
                    // REMOVED: BloodPressureRecord - not required by Google Play policy
                    // REMOVED: HeartRateRecord - not required by Google Play policy
                    HealthPermission.getReadPermission(WeightRecord::class),
                    HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
                )

                val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
                val permissionsToRequest = requiredPermissions - grantedPermissions

                if (permissionsToRequest.isEmpty()) {
                    Timber.d("All Health Connect permissions already granted")
                    sendResult(true)
                } else {
                    Timber.d("Requesting Health Connect permissions: $permissionsToRequest")
                    requestPermissions.launch(permissionsToRequest.toTypedArray())
                }
            } catch (e: Exception) {
                Timber.e(e, "Error requesting Health Connect permissions")
                sendResult(false)
            }
        }
    }

    private fun sendResult(granted: Boolean) {
        val result = if (granted) {
            GoogleFitAuthResult.Access
        } else {
            GoogleFitAuthResult.NotAccess
        }
        SingletonRxBusProvider.BUS.post(RxBus.Keys.SINGLE, result)
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}

