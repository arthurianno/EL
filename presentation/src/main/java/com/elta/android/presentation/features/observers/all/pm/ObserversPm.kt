package com.elta.android.presentation.features.observers.all.pm

import com.elta.android.domain.features.observers.interactor.GetObserverInvitesUseCase
import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.observers.all.mapper.ObserverMapper
import com.nullgr.core.rx.bindEmpty
import io.reactivex.Observable
import javax.inject.Inject

class ObserversPm @Inject constructor(
    private val mapper: ObserverMapper,
    private val getObserverInvitesUseCase: GetObserverInvitesUseCase,
    services: ServiceFacade
) : BaseListPm(services) {

    val inviteObserverAction = Action<Unit>()
    private val getObserversAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        bus.clicks<Clicks.ObserverItemClicked>()
            .map { it.item.id }
            .doOnNext { router.navigateTo(Screens.EditObserver(it)) }
            .subscribe()
            .untilDestroy()

        inviteObserverAction.observable
            .doOnNext { router.navigateTo(Screens.InviteObserver) }
            .subscribe()
            .untilDestroy()

        getObserversAction.observable
            .flatMap {
                getObserverInvitesUseCase.execute()
                    .hideErrorContainer()
                    .bindProgress()
                    .bindEmpty(emptyControl.visibilityState.consumer)
                    .doOnNext(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
            bus.events<Events.ObserverInvited>().map { Unit },
            bus.events<Events.ObserversUpdated>().map { Unit }
        )
            .subscribe(getObserversAction.consumer)
            .untilDestroy()
    }

    private fun handleSuccess(observers: List<Observer>) {
        items.consumer.accept(if (observers.isEmpty()) emptyList() else mapper.mapFromObject(observers))
    }
}