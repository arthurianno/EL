package com.elta.android.data.features.googlefit.datasource

import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.data.features.googlefit.dto.ActivityDto
import io.reactivex.Observable
import io.reactivex.Single

interface HealthAppDataSource {

    fun checkAuthorization(): Single<GoogleFitAuthResult>

    fun getActivities(): Observable<List<ActivityDto>>
}
