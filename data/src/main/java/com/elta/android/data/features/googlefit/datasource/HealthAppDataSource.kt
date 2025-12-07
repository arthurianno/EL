package com.elta.android.data.features.googlefit.datasource

import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.data.features.googlefit.dto.ActivityDto
import io.reactivex.Observable
import io.reactivex.Single

interface HealthAppDataSource {

    fun checkAuthorization(): Single<GoogleFitAuthResult>

    fun getActivities(): Observable<List<ActivityDto>>

    // Health metrics (available only on Android 14+ via Health Connect)
    fun getBloodGlucose(): Observable<List<BloodGlucoseRecord>> = Observable.just(emptyList())

    fun getWeight(): Observable<List<WeightRecord>> = Observable.just(emptyList())


    fun getTotalCaloriesBurned(): Observable<List<TotalCaloriesBurnedRecord>> = Observable.just(emptyList())
}
