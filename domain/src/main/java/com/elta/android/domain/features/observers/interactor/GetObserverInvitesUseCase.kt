package com.elta.android.domain.features.observers.interactor

import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.repository.ObserverRepository
import com.nullgr.core.interactor.ObservableListUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Observable
import javax.inject.Inject

class GetObserverInvitesUseCase @Inject constructor(
    private val repository: ObserverRepository,
    schedulers: SchedulersFacade
) : ObservableListUseCase<Observer, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Observable<List<Observer>> =
        repository.getObserverInvites()
}
