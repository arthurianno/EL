package com.elta.android.domain.features.observers.interactor

import com.elta.android.domain.features.observers.repository.ObserverRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class UpdateObserverNameUseCase @Inject constructor(
    private val repository: ObserverRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<UpdateObserverNameUseCase.Params>(schedulers) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        val p = checkNotNull(params)
        return repository.updateObserverName(p.id, p.name)
    }

    data class Params(val id: String, val name: String)
}