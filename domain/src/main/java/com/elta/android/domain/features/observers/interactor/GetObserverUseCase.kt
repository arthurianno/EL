package com.elta.android.domain.features.observers.interactor

import com.elta.android.domain.features.observers.model.Observer
import com.elta.android.domain.features.observers.repository.ObserverRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class GetObserverUseCase @Inject constructor(
    private val repository: ObserverRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Observer, GetObserverUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Single<Observer> =
        repository.getObserver(checkNotNull(params).id)

    data class Params(val id: String)
}
