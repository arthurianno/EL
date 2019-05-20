package com.elta.android.domain.features.user.interactor

import com.elta.android.domain.features.user.repository.ProfileRepository
import com.nullgr.core.interactor.SingleUseCase
import com.nullgr.core.rx.applySchedulers
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Single
import javax.inject.Inject

class IsOnboardingPassedUseCase @Inject constructor(
    private val userRepo: ProfileRepository,
    private val schedulers: SchedulersFacade
) : SingleUseCase<Boolean, Unit>(schedulers) {

    override fun buildUseCaseObservable(params: Unit?): Single<Boolean> =
        userRepo.isOnboardingPassed().applySchedulers(schedulers)
}