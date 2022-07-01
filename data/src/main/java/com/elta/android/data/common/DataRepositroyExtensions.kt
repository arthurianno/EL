package com.elta.android.data.common

import com.elta.android.common.errors.NetworkConnectionError
import com.elta.android.common.errors.ServiceUnavailableError
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.net.ConnectException
import java.net.SocketTimeoutException

fun <T> Observable<List<T>>.onConnectionErrorReturnsEmpty(): Observable<List<T>> =
    this.onErrorReturn {
        when (it.canIgnoreError()) {
            true -> emptyList()
            else -> throw it
        }
    }

fun <T> Single<List<T>>.onConnectionErrorReturnsEmpty(): Single<List<T>> =
    this.onErrorReturn {
        when (it.canIgnoreError()) {
            true -> emptyList()
            else -> throw it
        }
    }

fun <T> Observable<T>.onConnectionErrorResumeEmpty(): Observable<T> =
    this.onErrorResumeNext { error: Throwable ->
        when (error.canIgnoreError()) {
            true -> Observable.empty<T>()
            else -> Observable.error<T>(error)
        }
    }

fun <T> Single<T>.onConnectionErrorReturnsDefault(factory: () -> T): Single<T> =
    this.onErrorReturn {
        when (it.canIgnoreError()) {
            true -> factory.invoke()
            else -> throw it
        }
    }

fun <T> Single<T>.onConnectionErrorResumeDefault(factory: () -> Single<T>): Single<T> =
    this.onErrorResumeNext {
        when (it.canIgnoreError()) {
            true -> factory.invoke()
            else -> Single.error(it)
        }
    }

fun Completable.onConnectionErrorCompletes(): Completable =
    this.onErrorComplete { it.canIgnoreError() }

private fun Throwable.canIgnoreError() =
    this is NetworkConnectionError ||
        this is ServiceUnavailableError ||
        this is ConnectException ||
        this is SocketTimeoutException
