package com.elta.android.data.features.googlefit.datasource

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.currentMillis
import com.elta.android.common.utils.millisAtStartOfDay
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

/**
 * Health Connect data source - modern replacement for Google Fit
 * Works on Android 14+ (API 34+)
 */
class HealthConnectDataSource @Inject constructor(
    private val context: Context,
    private val syncStorage: SyncStorage,
    private val mapper: Mapper<ExerciseSessionRecord, ActivityDto>
) : HealthAppDataSource {

    private val healthConnectClient: HealthConnectClient? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    private val requiredPermissions = setOf(
        // Activity & Steps
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        // Health metrics
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
    )

    override fun checkAuthorization(): Single<GoogleFitAuthResult> {
        return Single.fromCallable {
            when {
                // Health Connect only available on Android 14+
                Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    Timber.d("Health Connect not available on Android < 14")
                    GoogleFitAuthResult.ApplicationNotInstalled
                }
                // Check if Health Connect is available
                healthConnectClient == null -> {
                    Timber.d("Health Connect client is null")
                    GoogleFitAuthResult.ApplicationNotInstalled
                }
                // Check if Health Connect app is installed
                !isHealthConnectAvailable() -> {
                    Timber.d("Health Connect app not installed")
                    GoogleFitAuthResult.ApplicationNotInstalled
                }
                // Check permissions
                else -> {
                    val hasPermissions = runBlocking {
                        checkPermissions()
                    }
                    if (hasPermissions) {
                        Timber.d("Health Connect permissions granted")
                        GoogleFitAuthResult.Access
                    } else {
                        Timber.d("Health Connect permissions not granted")
                        GoogleFitAuthResult.NotAccess
                    }
                }
            }
        }.doOnSuccess(::setInitialSyncTimeIfNeed)
    }

    override fun getActivities(): Observable<List<ActivityDto>> {
        return Observable.fromCallable {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Timber.w("Health Connect not available on this device")
                return@fromCallable emptyList<ActivityDto>()
            }

            val client = healthConnectClient ?: run {
                Timber.w("Health Connect client is null")
                return@fromCallable emptyList<ActivityDto>()
            }

            runBlocking {
                try {
                    val startTime = syncStorage.lastGoogleFitSync ?: millisAtStartOfDay()
                    val endTime = currentMillis()

                    Timber.d("Reading Health Connect activities from $startTime to $endTime")

                    val request = ReadRecordsRequest(
                        recordType = ExerciseSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.ofEpochMilli(startTime),
                            Instant.ofEpochMilli(endTime)
                        )
                    )

                    val response = client.readRecords(request)
                    val activities = response.records.mapNotNull { record ->
                        try {
                            mapper.mapFromObject(record)
                        } catch (e: Exception) {
                            Timber.e(e, "Error mapping exercise session")
                            null
                        }
                    }

                    syncStorage.lastGoogleFitSync = millisAtStartOfDay()
                    Timber.d("Successfully read ${activities.size} activities from Health Connect")
                    activities
                } catch (e: Exception) {
                    Timber.e(e, "Error reading Health Connect activities")
                    emptyList()
                }
            }
        }
    }

    private fun isHealthConnectAvailable(): Boolean {
        return try {
            val availability = HealthConnectClient.getSdkStatus(context)
            availability == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            Timber.e(e, "Error checking Health Connect availability")
            false
        }
    }

    private suspend fun checkPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            requiredPermissions.all { it in granted }
        } catch (e: Exception) {
            Timber.e(e, "Error checking Health Connect permissions")
            false
        }
    }

    private fun setInitialSyncTimeIfNeed(result: GoogleFitAuthResult) {
        if (result is GoogleFitAuthResult.Access && syncStorage.lastGoogleFitSync == null) {
            syncStorage.lastGoogleFitSync = millisAtStartOfDay()
        }
    }

    /**
     * Read blood glucose data from Health Connect
     */
    override fun getBloodGlucose(): Observable<List<BloodGlucoseRecord>> {
        return Observable.fromCallable {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return@fromCallable emptyList<BloodGlucoseRecord>()
            }

            val client = healthConnectClient ?: return@fromCallable emptyList<BloodGlucoseRecord>()

            runBlocking {
                try {
                    val startTime = syncStorage.lastGoogleFitSync ?: millisAtStartOfDay()
                    val endTime = currentMillis()

                    val request = ReadRecordsRequest(
                        recordType = BloodGlucoseRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.ofEpochMilli(startTime),
                            Instant.ofEpochMilli(endTime)
                        )
                    )

                    val response = client.readRecords(request)
                    Timber.d("Successfully read ${response.records.size} blood glucose records from Health Connect")
                    response.records
                } catch (e: Exception) {
                    Timber.e(e, "Error reading blood glucose from Health Connect")
                    emptyList()
                }
            }
        }
    }

    /**
     * Read blood pressure data from Health Connect
     */
    override fun getBloodPressure(): Observable<List<BloodPressureRecord>> {
        return Observable.fromCallable {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return@fromCallable emptyList<BloodPressureRecord>()
            }

            val client = healthConnectClient ?: return@fromCallable emptyList<BloodPressureRecord>()

            runBlocking {
                try {
                    val startTime = syncStorage.lastGoogleFitSync ?: millisAtStartOfDay()
                    val endTime = currentMillis()

                    val request = ReadRecordsRequest(
                        recordType = BloodPressureRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.ofEpochMilli(startTime),
                            Instant.ofEpochMilli(endTime)
                        )
                    )

                    val response = client.readRecords(request)
                    Timber.d("Successfully read ${response.records.size} blood pressure records from Health Connect")
                    response.records
                } catch (e: Exception) {
                    Timber.e(e, "Error reading blood pressure from Health Connect")
                    emptyList()
                }
            }
        }
    }

    /**
     * Read weight data from Health Connect
     */
    override fun getWeight(): Observable<List<WeightRecord>> {
        return Observable.fromCallable {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return@fromCallable emptyList<WeightRecord>()
            }

            val client = healthConnectClient ?: return@fromCallable emptyList<WeightRecord>()

            runBlocking {
                try {
                    val startTime = syncStorage.lastGoogleFitSync ?: millisAtStartOfDay()
                    val endTime = currentMillis()

                    val request = ReadRecordsRequest(
                        recordType = WeightRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.ofEpochMilli(startTime),
                            Instant.ofEpochMilli(endTime)
                        )
                    )

                    val response = client.readRecords(request)
                    Timber.d("Successfully read ${response.records.size} weight records from Health Connect")
                    response.records
                } catch (e: Exception) {
                    Timber.e(e, "Error reading weight from Health Connect")
                    emptyList()
                }
            }
        }
    }

    /**
     * Read heart rate data from Health Connect
     */
    override fun getHeartRate(): Observable<List<HeartRateRecord>> {
        return Observable.fromCallable {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return@fromCallable emptyList<HeartRateRecord>()
            }

            val client = healthConnectClient ?: return@fromCallable emptyList<HeartRateRecord>()

            runBlocking {
                try {
                    val startTime = syncStorage.lastGoogleFitSync ?: millisAtStartOfDay()
                    val endTime = currentMillis()

                    val request = ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.ofEpochMilli(startTime),
                            Instant.ofEpochMilli(endTime)
                        )
                    )

                    val response = client.readRecords(request)
                    Timber.d("Successfully read ${response.records.size} heart rate records from Health Connect")
                    response.records
                } catch (e: Exception) {
                    Timber.e(e, "Error reading heart rate from Health Connect")
                    emptyList()
                }
            }
        }
    }

    /**
     * Read total calories burned data from Health Connect
     */
    override fun getTotalCaloriesBurned(): Observable<List<TotalCaloriesBurnedRecord>> {
        return Observable.fromCallable {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                return@fromCallable emptyList<TotalCaloriesBurnedRecord>()
            }

            val client = healthConnectClient ?: return@fromCallable emptyList<TotalCaloriesBurnedRecord>()

            runBlocking {
                try {
                    val startTime = syncStorage.lastGoogleFitSync ?: millisAtStartOfDay()
                    val endTime = currentMillis()

                    val request = ReadRecordsRequest(
                        recordType = TotalCaloriesBurnedRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(
                            Instant.ofEpochMilli(startTime),
                            Instant.ofEpochMilli(endTime)
                        )
                    )

                    val response = client.readRecords(request)
                    Timber.d("Successfully read ${response.records.size} calories records from Health Connect")
                    response.records
                } catch (e: Exception) {
                    Timber.e(e, "Error reading calories from Health Connect")
                    emptyList()
                }
            }
        }
    }

    /**
     * Opens Health Connect app settings
     */
    fun openHealthConnectSettings() {
        try {
            val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error opening Health Connect settings")
            // Fallback: try to open in Play Store
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "market://details?id=com.google.android.apps.healthdata".let { Uri.parse(it) }
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Timber.e(e2, "Error opening Play Store for Health Connect")
            }
        }
    }
}

