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

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repository.sendObserverInvite(p.email, p.languageTag, p.countryCode)
    }

    data class Params(
        val email: String,
        val languageTag: String? = null,
        val countryCode: String? = null
    )
}
