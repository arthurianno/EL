package com.elta.android.data.features.googlefit.datasource

import android.content.Context
import android.content.pm.PackageManager
import com.elta.android.common.constants.GOOGLE_FIT_PACKAGE_NAME
import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.currentMillis
import com.elta.android.common.utils.millisAtStartOfDay
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.googlefit.datasource.utils.buildSessionsRequest
import com.elta.android.data.features.googlefit.datasource.utils.filterValidOnly
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.data.features.googlefit.datasource.utils.readSessions
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Session
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class GoogleFitDataSource @Inject constructor(
    private val context: Context,
    private val syncStorage: SyncStorage,
    private val mapper: Mapper<Session, ActivityDto>
) : HealthAppDataSource {

    override fun checkAuthorization(): Single<GoogleFitAuthResult> {
        return Single.fromCallable { launchGoogleFitAppAndPermissions(context) }
            .map { result ->
                when (result) {
                    GoogleFitAuthResult.ApplicationNotInstalled -> result
                    GoogleFitAuthResult.Access, GoogleFitAuthResult.NotAccess -> {
                        val hasPermission = GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(context), makeFitnessOptions())
                        if (hasPermission) GoogleFitAuthResult.Access else GoogleFitAuthResult.NotAccess
                    }
                }
            }
            .doOnSuccess(::setInitialSyncTimeIfNeed)
    }


    override fun getActivities(): Observable<List<ActivityDto>> =
        Observable.fromCallable {
            buildSessionsRequest(syncStorage.lastGoogleFitSync ?: currentMillis())
        }.flatMap {
            Fitness.getSessionsClient(context, checkNotNull(GoogleSignIn.getLastSignedInAccount(context)))
                .readSessions(it)
        }
            .map(mapper::mapFromObjects)
            .map { it.filterValidOnly() }
            .doOnNext { syncStorage.lastGoogleFitSync = millisAtStartOfDay() }

    private fun setInitialSyncTimeIfNeed(result: GoogleFitAuthResult) {
        if (result is GoogleFitAuthResult.Access && syncStorage.lastGoogleFitSync == null) {
            syncStorage.lastGoogleFitSync = millisAtStartOfDay()
        }
    }
}

private fun launchGoogleFitAppAndPermissions(context: Context): GoogleFitAuthResult {
    return try {
        context.packageManager.getPackageInfo(
            GOOGLE_FIT_PACKAGE_NAME,
            PackageManager.GET_ACTIVITIES
        )
        GoogleFitAuthResult.NotAccess
    } catch (ex: Exception) {
        GoogleFitAuthResult.ApplicationNotInstalled
    }

}

private fun makeFitnessOptions(): FitnessOptions =
    FitnessOptions.builder()
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
        .build()
