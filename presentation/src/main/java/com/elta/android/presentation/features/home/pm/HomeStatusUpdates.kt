package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.user.model.GlucoseFormat
import io.reactivex.Observable
import io.reactivex.Single

/** A failed refresh must not stop future home-screen updates. */
internal class HomeStatusUpdates(
    private val loadGlucoseFormat: () -> Single<GlucoseFormat>,
    private val loadFirstSync: () -> Single<Boolean>,
    private val onError: (Throwable) -> Unit
) {
    fun glucoseFormatOn(changes: Observable<Unit>): Observable<GlucoseFormat> =
        changes.flatMap {
            Observable.defer { loadGlucoseFormat().toObservable() }
                .doOnError(onError)
                .onErrorResumeNext(Observable.empty())
        }

    fun firstSyncOn(changes: Observable<Unit>): Observable<Boolean> =
        changes.flatMap {
            Observable.defer { loadFirstSync().toObservable() }
                .doOnError(onError)
                .onErrorResumeNext(Observable.empty())
        }
}
