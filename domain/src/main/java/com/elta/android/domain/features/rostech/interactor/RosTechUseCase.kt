package com.elta.android.domain.features.rostech.interactor

import com.elta.android.domain.features.rostech.repository.IomtRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class RosTechUseCase @Inject constructor(
    private val rosTech: IomtRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {
    override fun buildUseCaseObservable(params: Unit?): Completable {
        return rosTech.init()
    }

}