package com.elta.android.data.features.googlefit.datasource

import android.content.Context
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.googlefit.datasource.utils.RxGoogleFitAuthActivity
import com.elta.android.data.features.googlefit.datasource.utils.buildSessionsRequest
import com.elta.android.data.features.googlefit.datasource.utils.makeFitnessOptions
import com.elta.android.data.features.googlefit.datasource.utils.readSessions
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.Session
import io.reactivex.Observable
import java.util.Date
import javax.inject.Inject

class GoogleFitDataSource @Inject constructor(
    private val context: Context,
    private val syncStorage: SyncStorage,
    private val mapper: Mapper<Session, ActivityDto>
) : HealthAppDataSource {

    override fun checkAuthorization(): Observable<Boolean> =
        Observable.fromCallable {
            GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(context), makeFitnessOptions())
        }.flatMap {
            when (it) {
                true -> Observable.just(true)
                else -> RxGoogleFitAuthActivity.launchForResult(context)
                    .map { result -> result.success }
            }
        }
            .doOnNext(::setSyncTimeIfNeed)

    override fun getActivities(): Observable<List<ActivityDto>> =
        Observable.fromCallable {
            buildSessionsRequest(syncStorage.lastGoogleFitSync ?: Date().time)
        }.flatMap {
            Fitness.getSessionsClient(context, checkNotNull(GoogleSignIn.getLastSignedInAccount(context)))
                .readSessions(it)
        }
            .map(mapper::mapFromObjects)
            .doOnNext { syncStorage.lastGoogleFitSync = Date().time }


    private fun setSyncTimeIfNeed(result: Boolean) {
        if (result && syncStorage.lastGoogleFitSync == null) {
            syncStorage.lastGoogleFitSync = Date().time
        }
    }
}