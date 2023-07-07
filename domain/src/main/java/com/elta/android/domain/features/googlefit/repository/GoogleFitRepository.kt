package com.elta.android.domain.features.googlefit.repository

import com.elta.android.domain.features.googlefit.model.GoogleFitAuthResult
import io.reactivex.Completable
import io.reactivex.Single

interface GoogleFitRepository {

    fun checkAuthorization(): Single<GoogleFitAuthResult>

    fun sync(): Completable
}
