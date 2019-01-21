package com.elta.android.data.common

import com.elta.android.common.errors.NetworkConnectionError
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

fun <R> Observable<R>.checkNetwork(checker: NetworkChecker): Observable<R> =
    if (checker.isInternetConnectionEnabled()) this
    else Observable.error(NetworkConnectionError())

fun <R> Single<R>.checkNetwork(checker: NetworkChecker): Single<R> =
    if (checker.isInternetConnectionEnabled()) this
    else Single.error(NetworkConnectionError())

fun Completable.checkNetwork(networkChecker: NetworkChecker): Completable =
    if (networkChecker.isInternetConnectionEnabled()) this
    else Completable.error(NetworkConnectionError())