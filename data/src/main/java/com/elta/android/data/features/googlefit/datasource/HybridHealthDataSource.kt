package com.elta.android.data.features.googlefit.datasource

import android.content.Context
import android.os.Build
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * Hybrid Health App DataSource that uses:
 * - Health Connect API on Android 14+ (preferred)
 * - Google Fit API on Android 13 and below (fallback)
 *
 * This ensures compatibility across all Android versions while
 * using the modern Health Connect API when available.
 */
class HybridHealthDataSource @Inject constructor(
    private val healthConnectDataSource: HealthConnectDataSource,
    private val googleFitDataSource: GoogleFitDataSource
) : HealthAppDataSource {

    override fun checkAuthorization(): Single<GoogleFitAuthResult> {
        return if (isHealthConnectPreferred()) {
            Timber.d("Using Health Connect for authorization check")
            healthConnectDataSource.checkAuthorization()
                .onErrorResumeNext { error ->
                    Timber.w(error, "Health Connect check failed, falling back to Google Fit")
                    googleFitDataSource.checkAuthorization()
                }
        } else {
            Timber.d("Using Google Fit for authorization check")
            googleFitDataSource.checkAuthorization()
        }
    }

    override fun getActivities(): Observable<List<ActivityDto>> {
        return if (isHealthConnectPreferred()) {
            Timber.d("Using Health Connect to get activities")
            healthConnectDataSource.getActivities()
                .onErrorResumeNext { error: Throwable ->
                    Timber.w(error, "Health Connect read failed, falling back to Google Fit")
                    googleFitDataSource.getActivities()
                }
        } else {
            Timber.d("Using Google Fit to get activities")
            googleFitDataSource.getActivities()
        }
    }

    /**
     * Determines if Health Connect should be used
     * - Android 14+ (API 34+): Use Health Connect
     * - Android 13 and below: Use Google Fit
     */
    private fun isHealthConnectPreferred(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    /**
     * Read blood glucose data (Health Connect only, Android 14+)
     */
    override fun getBloodGlucose() = healthConnectDataSource.getBloodGlucose()


    /**
     * Read weight data (Health Connect only, Android 14+)
     */
    override fun getWeight() = healthConnectDataSource.getWeight()


    /**
     * Read total calories burned data (Health Connect only, Android 14+)
     */
    override fun getTotalCaloriesBurned() = healthConnectDataSource.getTotalCaloriesBurned()

    /**
     * Get current active health app name for UI display
     */
    fun getActiveHealthAppName(): String {
        return if (isHealthConnectPreferred()) {
            "Health Connect"
        } else {
            "Google Fit"
        }
    }
}

