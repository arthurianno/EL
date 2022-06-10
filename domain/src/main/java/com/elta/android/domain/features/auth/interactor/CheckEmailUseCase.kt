package com.elta.android.domain.features.auth.interactor

import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class CheckEmailUseCase @Inject constructor(
    private val repository: AuthRepository,
    schedulers: SchedulersFacade
) : SingleUseCase<Boolean, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<Boolean> = repository.isEmailConfirmed()
}
