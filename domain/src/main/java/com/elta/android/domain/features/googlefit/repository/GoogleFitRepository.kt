package com.elta.android.domain.features.googlefit.repository

import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import com.elta.android.domain.features.googlefit.model.HealthMetrics
import io.reactivex.Completable
import io.reactivex.Single

interface GoogleFitRepository {

    fun checkAuthorization(): Single<GoogleFitAuthResult>

    fun sync(): Completable

    /**
     * Sync all health metrics from Health Connect (Android 14+)
     * Returns HealthMetrics with all available data
     */
    fun syncHealthMetrics(): Single<HealthMetrics>
}
