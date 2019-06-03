package com.elta.android.data.features.googlefit.datasource

import com.elta.android.data.features.googlefit.dto.ActivityDto
import io.reactivex.Observable

interface HealthAppDataSource {

    fun checkAuthorization(): Observable<Boolean>

    fun getActivities(): Observable<List<ActivityDto>>
}