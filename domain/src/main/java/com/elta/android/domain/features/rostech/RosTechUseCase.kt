package com.elta.android.domain.features.rostech

import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class RosTechUseCase @Inject constructor(
    private val rosTech: RosTechRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Completable {
        return rosTech.init()
    }

}