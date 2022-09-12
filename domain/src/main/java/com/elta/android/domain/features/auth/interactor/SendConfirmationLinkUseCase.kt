package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.interactor.CompletableUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import javax.inject.Inject

class SendConfirmationLinkUseCase @Inject constructor(
    private val repository: AuthRepository,
    schedulers: SchedulersFacade
) : CompletableUseCase<Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Completable = repository.sendConfirmationLink()
}
