package com.elta.android.domain.features.observers.interactor

import com.elta.android.domain.features.observers.repository.ObserverRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SendObserverInviteUseCase @Inject constructor(
    private val repository: ObserverRepository,
    schedulersFacade: SchedulersFacade
) : CompletableUseCase<SendObserverInviteUseCase.Params>(schedulersFacade) {

    override fun buildUseCaseObservable(params: Params?): Completable =
        repository.sendObserverInvite(checkNotNull(params).email)

    data class Params(val email: String)
}