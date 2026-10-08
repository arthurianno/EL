package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.sync.interactor.SyncLocalChangesUseCase
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.nullgr.core.rx.RxBus
import com.nullgr.core.rx.bindProgress
import io.reactivex.Completable
import io.reactivex.functions.Consumer

internal class HomeBackendSyncRunner(
    private val syncLocalChanges: SyncLocalChangesUseCase,
    private val connectIomt: () -> Completable,
    private val bus: RxBus
) {
    fun syncAll(progress: Consumer<Boolean>): Completable =
        syncLocalChanges.execute()
            .mergeWith(connectIomt())
            .bindProgress(progress)
            .doOnSubscribe { bus.event(Events.Sync.Server.Started) }
            .doOnComplete { bus.event(Events.Sync.Server.Success) }
            .doOnComplete {
                bus.event(Events.EventsChanged(false))
                bus.event(Events.ProfileUpdated)
            }

    fun syncIomt(progress: Consumer<Boolean>): Completable =
        connectIomt()
            .bindProgress(progress)
            .doOnSubscribe { bus.event(Events.Sync.Server.Started) }
            .doOnComplete { bus.event(Events.Sync.Server.Success) }
}
