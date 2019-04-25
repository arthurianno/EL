package com.elta.android.domain.features.observers.interactor

import com.elta.android.domain.features.observers.repository.ObserverRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class DeleteObserverUseCase @Inject constructor(
    private val repository: ObserverRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<DeleteObserverUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repository.deleteObserverInvite(checkNotNull(params).id)

    data class Params(val id: String)
}