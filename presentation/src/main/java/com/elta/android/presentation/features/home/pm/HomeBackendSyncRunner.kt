package com.elta.android.presentation.features.home.pm

import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.bindProgress
import io.reactivex.Completable
import io.reactivex.functions.Consumer

internal class HomeBackendSyncRunner(
    private val syncLocalChanges: () -> Completable,
    private val connectIomt: () -> Completable,
    private val bus: RxBus
) {
    fun syncAll(progress: Consumer<Boolean>, reportError: (Throwable) -> Unit): Completable =
        Completable.defer { syncLocalChanges().mergeWith(connectIomt()) }
            .bindProgress(progress)
            .doOnSubscribe { bus.event(Events.Sync.Server.Started) }
            .doOnComplete { bus.event(Events.Sync.Server.Success) }
            .doOnComplete {
                bus.event(Events.EventsChanged(false))
                bus.event(Events.ProfileUpdated)
            }
            .doOnError(reportError)
            .onErrorComplete()
}
