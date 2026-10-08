package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.userinfo.model.UserInfo
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles

internal class HomeAutoSyncRunner(
    private val loadUserInfo: () -> Single<UserInfo>,
    private val hasPrimaryDevice: () -> Single<Boolean>,
    private val syncDevice: () -> Observable<Unit>,
    private val onDeviceError: (Throwable) -> Unit,
    private val startBackendSync: () -> Unit
) {
    fun execute(): Observable<Unit> =
        Singles.zip(loadUserInfo(), hasPrimaryDevice())
            .flatMapObservable { (info, hasPrimary) ->
                if (hasPrimary && info.isFirstHomeEntrance != true) {
                    syncDevice()
                        .doOnError { onDeviceError(it) }
                        .doOnComplete { startBackendSync() }
                } else {
                    Observable.fromCallable {
                        startBackendSync()
                    }
                }
            }
}
