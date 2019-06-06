package com.elta.android.domain.features.googlefit.repository

import io.reactivex.Completable
import io.reactivex.Observable

interface GoogleFitRepository {

    fun checkAuthorization(): Observable<Boolean>

    fun sync(): Completable
}